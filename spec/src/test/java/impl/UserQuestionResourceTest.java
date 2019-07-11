package impl;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.User;
import api.UserQuestionResource;
import api.UserResource;
import api.auth.Auth;
import dao.QuestionVoteDao;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;
import java.util.function.BiFunction;

import static impl.TestSetup.getQuestion;
import static impl.TestSetup.insertUser;
import static impl.UserAnswerResourceImpl.INVALID_VOTE;
import static impl.UserQuestionResourceImpl.QUESTION_NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserQuestionResourceTest {

    private static UserQuestionResource userQuestionResource;
    private static QuestionResource     questionResource;
    private static UserResource         userResource;
    private static AnswerResource       answerResource;
    private static QuestionVoteDao      questionVoteDao;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static TestSetup testSetup;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        userQuestionResource = testSetup.getInjector().getInstance(UserQuestionResource.class);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        answerResource = testSetup.getInjector().getInstance(AnswerResource.class);
        questionVoteDao = testSetup.getInjector().getInstance(QuestionVoteDao.class);
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
    public void shouldBePossibleToAddQuestionAndFetchIt() {

        User createdUser = insertUser(userResource);

        // when question is created
        Question question = getQuestion("my question title", "my question");
        Auth     mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());

        // when we create the question
        questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);

        // then the question should be returned when asking for the users questions
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();
        assertEquals(1, questions.size());

        Question insertedQuestion = questions.get(0);
        assertEquals("my question title", insertedQuestion.getTitle());
        assertEquals("my question", insertedQuestion.getQuestion());
        assertEquals(question.getBounty(), insertedQuestion.getBounty());
        assertEquals(createdUser.getId(), insertedQuestion.getUserId());
        assertNotNull(insertedQuestion.getId());
    }

    @Test
    public void shouldBePossibleToGetQuestionById() {

        // when question is inserted
        Question question = getQuestion("my question title", "my question");
        Auth     mockAuth = newAuth();
        mockAuth.setUserId(mockAuth.getUserId());
        questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);
        List<Question> questions = userQuestionResource.getQuestions(mockAuth.getUserId()).toBlocking().single();
        assertEquals(1, questions.size());

        // then the question should be returned when asking for the specific question
        Question selectedQuestion = userQuestionResource.getQuestion(newAuth(), questions.get(0).getId()).toBlocking().single();

        assertEquals("my question title", selectedQuestion.getTitle());
        assertEquals("my question", selectedQuestion.getQuestion());
        assertEquals(Integer.valueOf(300), selectedQuestion.getBounty());
        assertEquals(mockAuth.getUserId(), selectedQuestion.getUserId().longValue());
    }

    @Test
    public void shouldBePossibleToUpdateQuestion() {

        User createdUser = insertUser(userResource);

        // when question is inserted
        Question question = getQuestion("my question title", "my question");

        Auth mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());
        questionResource.createQuestion(mockAuth, question).toBlocking().singleOrDefault(null);

        Question storedQuestion = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single().get(0);
        storedQuestion.setBounty(400);
        storedQuestion.setTitle("new title");
        storedQuestion.setQuestion("new question body");
        userQuestionResource.updateQuestion(mockAuth, storedQuestion.getId(), storedQuestion).toBlocking().singleOrDefault(null);
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();
        assertEquals(1, questions.size());

        Question updatedQuestion = questions.get(0);
        assertEquals("new title", updatedQuestion.getTitle());
        assertEquals("new question body", updatedQuestion.getQuestion());
        assertEquals(question.getBounty(), updatedQuestion.getBounty());
        assertEquals(createdUser.getId(), updatedQuestion.getUserId());
    }

    @Test
    public void shouldOnyReturnQuestionsForTheSpecifiedUser() {

        User otherUser = insertUser(userResource);
        User ourUser   = insertUser(userResource);

        // when questions are inserted for different users
        Question ourQuestion = getQuestion("our users question title", "our users question");

        // when question is inserted
        Question questionForOtherUser = getQuestion("other users question title", "other users question");

        Auth auth = new MockAuth(ourUser.getId());
        questionResource.createQuestion(auth, ourQuestion).toBlocking().singleOrDefault(null);
        Auth authOtherUser = new MockAuth(otherUser.getId());
        questionResource.createQuestion(authOtherUser, questionForOtherUser).toBlocking().singleOrDefault(null);

        // then only questions for our user should be returned
        List<Question> questions = userQuestionResource.getQuestions(ourUser.getId()).toBlocking().single();
        assertEquals(1, questions.size());

        Question insertedQuestion = questions.get(0);
        assertEquals("our users question title", insertedQuestion.getTitle());
        assertEquals(ourUser.getId(), insertedQuestion.getUserId());
    }

    @Test
    public void shouldBePossibleToDeleteQuestion() {

        User createdUser = insertUser(userResource);

        Auth mockAuth = new MockAuth(createdUser.getId());
        mockAuth.setUserId(createdUser.getId());

        // given questions exists
        Question questionToInsert = getQuestion("my question title", "my question");

        Question questionToInsert2 = getQuestion("my question title2", "my question2");

        questionResource.createQuestion(mockAuth, questionToInsert).toBlocking().singleOrDefault(null);
        questionResource.createQuestion(mockAuth, questionToInsert2).toBlocking().singleOrDefault(null);

        // and the questions has answers
        List<Question> questions = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();

        Answer answer = new Answer();
        answer.setAnswer("just a answer");
        answerResource.answerQuestion(mockAuth, answer, questions.get(0).getId()).toBlocking().singleOrDefault(null);
        answerResource.answerQuestion(mockAuth, answer, questions.get(1).getId()).toBlocking().singleOrDefault(null);

        List<Question> questionsSaved = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();

        // when we deletes a question
        userQuestionResource.deleteQuestion(mockAuth, questionsSaved.get(0).getId()).toBlocking().singleOrDefault(null);

        // only the question we want to delete should be removed
        List<Question> remaningQuestions = userQuestionResource.getQuestions(createdUser.getId()).toBlocking().single();
        assertThat(remaningQuestions.size()).isEqualTo(1);
        assertThat(remaningQuestions.get(0).getTitle()).isEqualTo("my question title2");

        // and the answers should be deleted as well for the deleted question
        List<Answer> answersForTheNonDeletedQuestion = answerResource.getAnswers(mockAuth, questionsSaved.get(1).getId()).toBlocking().singleOrDefault(null);
        assertThat(answersForTheNonDeletedQuestion).isNotEmpty();

        List<Answer> answersForTheDeletedQuestion = answerResource.getAnswers(mockAuth, questionsSaved.get(0).getId()).toBlocking().singleOrDefault(null);
        assertThat(answersForTheDeletedQuestion).isEmpty();
    }

    @Test
    public void shouldNotDeleteQuestionThatDoesNotExist() {
        User createdUser = insertUser(userResource);

        Auth auth                  = new MockAuth(createdUser.getId());
        long nonExistingQuestionId = 12;
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userQuestionResource.deleteQuestion(auth, nonExistingQuestionId).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(NOT_FOUND, e.getStatus());
                assertEquals(QUESTION_NOT_FOUND, e.getError());
            });
    }

    @Test
    public void shouldYieldCorrectVotesWhenDownVotingAndUpVoting() {

        Auth auth1 = newAuth();
        Auth auth2 = newAuth();
        Auth auth3 = newAuth();

        Question question = questionResource.createQuestion(newAuth(), getQuestion("my question title", "my question")).toBlocking().singleOrDefault(null);

        voteAndAssertSuccess(userQuestionResource::downVoteQuestion, auth1, question, -1);
        voteAndAssertFailure(userQuestionResource::downVoteQuestion, auth1, question);
        voteAndAssertSuccess(userQuestionResource::upVoteQuestion, auth1, question, 0);
        assertNoVote(auth1, question);
        voteAndAssertSuccess(userQuestionResource::upVoteQuestion, auth1, question, 1);
        voteAndAssertFailure(userQuestionResource::upVoteQuestion, auth1, question);

        voteAndAssertSuccess(userQuestionResource::upVoteQuestion, auth2, question, 2);

        voteAndAssertSuccess(userQuestionResource::upVoteQuestion, auth3, question, 3);

        voteAndAssertSuccess(userQuestionResource::downVoteQuestion, auth2, question, 2);
        assertNoVote(auth2, question);

        voteAndAssertSuccess(userQuestionResource::downVoteQuestion, auth3, question, 1);
        assertNoVote(auth2, question);
    }

    @Test
    public void shouldThrowErrorWhenUserVotesOnOwnAnswer() {

        Auth     auth     = newAuth();
        Question question = questionResource.createQuestion(auth, getQuestion("my question title", "my question")).toBlocking().singleOrDefault(null);

        voteAndAssertFailure(userQuestionResource::downVoteQuestion, auth, question);
        voteAndAssertFailure(userQuestionResource::upVoteQuestion, auth, question);
    }

    private void voteAndAssertSuccess(BiFunction<Auth, Long, Observable<Void>> call, Auth auth, Question question, int expectedVotes) {
        call.apply(auth, question.getId()).test()
            .awaitTerminalEvent()
            .assertNoErrors();
        assertThat(questionResource.getQuestionById(question.getId()).test().awaitTerminalEvent().getOnNextEvents())
            .extracting(Question::getVotes)
            .containsExactly(expectedVotes);
    }

    private void voteAndAssertFailure(BiFunction<Auth, Long, Observable<Void>> call, Auth auth, Question question) {
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> call.apply(auth, question.getId()).toBlocking().single())
            .satisfies(e -> {
                assertThat(e).isInstanceOf(WebException.class);
                assertThat(e).hasFieldOrPropertyWithValue("status", BAD_REQUEST);
                assertThat(e).hasFieldOrPropertyWithValue("error", INVALID_VOTE);
            });
    }

    private void assertNoVote(Auth auth, Question answer) {
        questionVoteDao.findVote(auth.getUserId(), answer.getId()).test().awaitTerminalEvent()
            .assertNoValues();
    }

    private Auth newAuth() {
        User createdUser = insertUser(userResource);
        return new MockAuth(createdUser.getId());
    }
}
