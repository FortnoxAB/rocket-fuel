package impl;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.User;
import api.UserAnswerResource;
import api.UserQuestionResource;
import api.UserResource;
import api.auth.Auth;
import dao.VoteDao;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import static impl.UserAnswerResourceImpl.ANSWER_NOT_FOUND;
import static impl.UserAnswerResourceImpl.INVALID_VOTE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static util.ObservableAssertions.assertThat;
import static util.ObservableAssertions.assertThatExceptionOfType;
import static util.ObservableAssertions.assertThatList;

public class UserAnswerResourceTest {

    private static UserQuestionResource userQuestionResource;
    private static QuestionResource     questionResource;
    private static UserResource         userResource;
    private static UserAnswerResource   userAnswerResource;
    private static AnswerResource       answerResource;
    private static VoteDao              voteDao;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        userQuestionResource = testSetup.getInjector().getInstance(UserQuestionResource.class);
        userAnswerResource = testSetup.getInjector().getInstance(UserAnswerResource.class);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        answerResource = testSetup.getInjector().getInstance(AnswerResource.class);
        voteDao = testSetup.getInjector().getInstance(VoteDao.class);
    }

    @After
    public void afterEach() throws Exception {
        testSetup.clearDatabase();
    }

    @Before
    public void beforeEach() throws Exception {
        testSetup.setupDatabase();
    }

    @Test
    public void shouldAddAnswerToQuestion() {

        Auth     createdUserAuth = newAuth();
        Question question        = createQuestion(createdUserAuth);

        Answer answer = createAnswer();

        assertThat(answerResource.answerQuestion(createdUserAuth, answer, question.getId()))
            .hasExactlyOne()
            .extracting(Answer::getId)
            .isNotNull();

        Answer createdAnswer = getFirstAnswer(createdUserAuth, question);
        assertFalse(createdAnswer.isAccepted());
        assertEquals("this is the body of the answer", createdAnswer.getAnswer());
        assertThat(createdAnswer.getVotes()).isZero();
        assertEquals("Test Subject", createdAnswer.getCreatedBy());
    }

    @Test
    public void shouldBeAbleToUpdateAnswer() {

        Auth     createdUserAuth = newAuth();
        Question question        = createQuestion(createdUserAuth);

        Answer answer = new Answer();
        answer.setAnswer("this is the body of the answer");

        assertThat(answerResource.answerQuestion(createdUserAuth, answer, question.getId()))
            .hasExactlyOne();

        Answer updatedAnswerInput = new Answer();
        updatedAnswerInput.setAnswer("this is an updated body of the answer");
        updatedAnswerInput.setAccepted(false);

        assertThat(userAnswerResource.updateAnswer(createdUserAuth, answer.getId(), updatedAnswerInput))
            .isEmpty();

        Answer updatedAnswer = getFirstAnswer(createdUserAuth, question);

        assertFalse(updatedAnswer.isAccepted());
        assertEquals("this is an updated body of the answer", updatedAnswer.getAnswer());
        assertEquals("Test Subject", updatedAnswer.getCreatedBy());
    }

    @Test
    public void shouldBeAbleToDeleteAnswer() {
        // given answers exists
        Auth     auth     = newAuth();
        Question question = createQuestion(auth);

        Answer answer  = createAnswer();
        Answer answer2 = createAnswer("title2", "answer2");

        assertThat(answerResource.answerQuestion(auth, answer, question.getId()))
            .hasExactlyOne();
        assertThat(answerResource.answerQuestion(auth, answer2, question.getId()))
            .hasExactlyOne();

        Answer createdAnswer = getFirstAnswer(auth, question);

        // when we delete a answer
        assertThat(userAnswerResource.deleteAnswer(auth, createdAnswer.getId()))
            .isEmpty();

        // then only the answer we want to delete should be deleted
        assertThatList(userAnswerResource.getAnswers(auth.getUserId(), question.getId()))
            .hasExactlyOne()
            .hasSize(1);
    }

    @Test
    public void shouldNotDeleteAnswerThatDoesNotExist() {
        Auth auth                = newAuth();
        long nonExistingAnswerId = 12;

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userAnswerResource.deleteAnswer(auth, nonExistingAnswerId))
            .satisfies(e -> {
                assertEquals(NOT_FOUND, e.getStatus());
                assertEquals(ANSWER_NOT_FOUND, e.getError());
            });
    }

    @Test
    public void shouldBeAbleToFetchAnswersForGivenUserAndQuestion() {

        Auth     authUserOne = newAuth();
        Question question    = createQuestion(authUserOne);

        Answer answer = createAnswer();

        Auth authUserTwo = newAuth();

        Question questionForUserTwo = createQuestion(authUserTwo);

        Answer answerForUserTwo = createAnswer();
        assertThat(answerResource.answerQuestion(authUserOne, answer, question.getId()))
            .hasExactlyOne();
        assertThat(answerResource.answerQuestion(authUserTwo, answerForUserTwo, questionForUserTwo.getId()))
            .hasExactlyOne();

        Answer createdAnswer = getFirstAnswer(authUserOne, question);
        assertFalse(createdAnswer.isAccepted());
        assertEquals("this is the body of the answer", createdAnswer.getAnswer());
        assertThat(createdAnswer.getVotes()).isZero();
        assertEquals("Test Subject", createdAnswer.getCreatedBy());
    }

    @Test
    public void shouldYieldCorrectVotesWhenDownVotingAndUpVoting() {

        Auth   auth1  = newAuth();
        Auth   auth2  = newAuth();
        Auth   auth3  = newAuth();
        Answer answer = createQuestionAndAnswer(newAuth());

        voteAndAssertSuccess(userAnswerResource::downVoteAnswer, auth1, answer, -1);
        voteAndAssertFailure(userAnswerResource::downVoteAnswer, auth1, answer);
        voteAndAssertSuccess(userAnswerResource::upVoteAnswer, auth1, answer, 0);
        assertNoVote(auth1, answer);
        voteAndAssertSuccess(userAnswerResource::upVoteAnswer, auth1, answer, 1);
        voteAndAssertFailure(userAnswerResource::upVoteAnswer, auth1, answer);

        voteAndAssertSuccess(userAnswerResource::upVoteAnswer, auth2, answer, 2);

        voteAndAssertSuccess(userAnswerResource::upVoteAnswer, auth3, answer, 3);

        voteAndAssertSuccess(userAnswerResource::downVoteAnswer, auth2, answer, 2);
        assertNoVote(auth2, answer);

        voteAndAssertSuccess(userAnswerResource::downVoteAnswer, auth3, answer, 1);
        assertNoVote(auth2, answer);
    }

    @Test
    public void shouldThrowErrorWhenUserVotesOnOwnAnswer() {

        Auth   auth   = newAuth();
        Answer answer = createQuestionAndAnswer(auth);

        voteAndAssertFailure(userAnswerResource::downVoteAnswer, auth, answer);
        voteAndAssertFailure(userAnswerResource::upVoteAnswer, auth, answer);
    }

    private void voteAndAssertSuccess(BiFunction<Auth, Long, Observable<Void>> call, Auth auth, Answer answer, int expectedVotes) {
        assertThat(call.apply(auth, answer.getId()))
            .isEmpty();
        assertThat(answerResource.getAnswerById(answer.getId()))
            .hasExactlyOne()
            .extracting(Answer::getVotes)
            .isEqualTo(expectedVotes);
    }

    private void voteAndAssertFailure(BiFunction<Auth, Long, Observable<Void>> call, Auth auth, Answer answer) {
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> call.apply(auth, answer.getId()).toBlocking().single())
            .satisfies(e -> {
                assertThat(e).isInstanceOf(WebException.class);
                assertThat(e).hasFieldOrPropertyWithValue("status", BAD_REQUEST);
                assertThat(e).hasFieldOrPropertyWithValue("error", INVALID_VOTE);
            });
    }

    private void assertNoVote(Auth auth, Answer answer) {
        assertThat(voteDao.findVote(auth.getUserId(), answer.getId()))
            .isEmpty();
    }

    private Answer createQuestionAndAnswer(Auth auth) {
        Question question = createQuestion(auth);
        Answer   answer   = new Answer();
        answer.setAnswer("answer");
        return answerResource.answerQuestion(auth, answer, question.getId()).toBlocking().singleOrDefault(null);
    }

    private Answer getFirstAnswer(Auth auth, Question question) {
        List<Answer> createdAnswers = userAnswerResource.getAnswers(auth.getUserId(), question.getId()).toBlocking().single();
        assertThat(createdAnswers).isNotEmpty();
        return createdAnswers.get(0);
    }

    private Question createQuestion(Auth auth) {
        // when question is inserted
        Question question = new Question();
        question.setAnswerAccepted(false);
        question.setBounty(300);
        question.setTitle("my question title");
        question.setVotes(3);
        question.setQuestion("my question");

        questionResource.createQuestion(new MockAuth(auth.getUserId()), question).toBlocking().singleOrDefault(null);

        // then the question should be returned when asking for the users questions
        List<Question> questions = userQuestionResource.getQuestions(auth.getUserId()).toBlocking().single();
        assertEquals(1, questions.size());
        return questions.get(0);
    }

    private Auth newAuth() {
        final String generatedEmail = UUID.randomUUID().toString() + "@fortnox.se";
        User         user           = new User();
        user.setEmail(generatedEmail);
        user.setName("Test Subject");
        userResource.createUser(null, user).toBlocking().single();
        return new MockAuth(userResource.getUserByEmail(generatedEmail, false).toBlocking().single().getId());
    }

    private static Answer createAnswer() {
        return createAnswer("this is an answer title", "this is the body of the answer");
    }

    private static Answer createAnswer(String title, String answerBody) {
        Answer answer = new Answer();
        answer.setAnswer(answerBody);
        return answer;
    }
}
