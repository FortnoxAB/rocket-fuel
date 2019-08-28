package impl;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.User;
import api.UserResource;
import api.auth.Auth;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.google.inject.AbstractModule;
import dao.AnswerDao;
import dao.AnswerVote;
import dao.AnswerVoteDao;
import org.assertj.core.groups.Tuple;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import rx.Observable;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.jaxrs.WebException;
import slack.SlackResource;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static impl.AnswerResourceImpl.ANSWER_NOT_FOUND;
import static impl.AnswerResourceImpl.ERROR_ANSWER_NOT_CREATED;
import static impl.AnswerResourceImpl.ERROR_NOT_OWNER_OF_QUESTION;
import static impl.AnswerResourceImpl.INVALID_VOTE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.error;
import static rx.Observable.just;
import static se.fortnox.reactivewizard.test.TestUtil.matches;
import static util.ObservableAssertions.assertThat;
import static util.ObservableAssertions.assertThatExceptionOfType;
import static util.ObservableAssertions.assertThatList;

public class AnswerResourceTest {
    private static final String           SLACK_USER_ID = "U0G9QF9C6";
    private static       QuestionResource questionResource;
    private static       AnswerResource   answerResource;

    @ClassRule
    public static  PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();
    private static TestSetup           testSetup;
    private static UserResource        userResource;
    private static SlackResource       mockedSlackResource;
    private static AnswerVoteDao       answerVoteDao;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer, new AbstractModule() {
            @Override
            protected void configure() {
                ApplicationConfig config = new ApplicationConfig();
                config.setBaseUrl("https://fuel.fnox.se");

                binder().bind(ApplicationConfig.class).toInstance(config);
            }
        });
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        answerResource = testSetup.getInjector().getInstance(AnswerResource.class);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
        mockedSlackResource = testSetup.getInjector().getInstance(SlackResource.class);
        answerVoteDao = testSetup.getInjector().getInstance(AnswerVoteDao.class);
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
    public void shouldMarkBothQuestionAndAnswerAsAcceptedWhenUserAcceptsAnAnswer() {
        // given user creates a question

        Auth questioner = newUser();
        when(mockedSlackResource.getUserId(questioner.getEmail())).thenReturn(just(SLACK_USER_ID));

        Question question = newQuestion();

        Question returnedQuestion = questionResource.createQuestion(questioner, question).toBlocking().singleOrDefault(null);
        assertThat(returnedQuestion).isNotNull();
        assertThat(returnedQuestion.getId()).isGreaterThan(0);

        ApplicationConfig applicationConfig = testSetup.getInjector().getInstance(ApplicationConfig.class);

        Auth answerer = newUser();
        // and someone answers the question
        Answer answer         = newAnswer();
        Answer returnedAnswer = answerResource.createAnswer(answerer, answer, returnedQuestion.getId()).toBlocking().singleOrDefault(null);
        assertThat(returnedAnswer).isNotNull();

        //Verify slack notification is send to the user who created the question
        verify(mockedSlackResource).getUserId(questioner.getEmail());
        verify(mockedSlackResource).postMessageToSlackAsBotUser(eq(SLACK_USER_ID), matches(layoutBlocks -> {
            assertThat(layoutBlocks).hasSize(3);

            assertThat(layoutBlocks.get(0))
                .extracting("text.text")
                .containsExactly(format("Your question: *%s* got an answer:", question.getTitle()));

            assertThat(layoutBlocks.get(1))
                .extracting("text.text")
                .containsExactly(answer.getAnswer());

            assertThat(layoutBlocks.get(2))
                .extracting("text.text")
                .containsExactly(format("Head over to <%s|rocket-fuel> to accept the answer", applicationConfig.getBaseUrl() + "/question/" + question.getId() + "#answer_" + answer.getId()));
        }));

        // when the creator of the question marks a answer as the correct one
        answerResource.markAsAcceptedAnswer(questioner, returnedAnswer.getId()).toBlocking().singleOrDefault(null);

        // then both answer and question should be updated and state that they are accepted.
        List<Answer> answers = answerResource.getAnswers(questioner, returnedQuestion.getId()).toBlocking().singleOrDefault(new ArrayList<>());
        assertThat(answers)
            .hasOnlyOneElementSatisfying(acceptedAnswer -> {
                assertThat(acceptedAnswer.isAccepted()).isTrue();
                assertThat(acceptedAnswer.getAcceptedAt()).isCloseTo(now(), within(1, SECONDS));
                assertThat(acceptedAnswer.getVotes()).isZero();
                assertThat(acceptedAnswer.getCurrentUserVote()).isZero();
            });
        assertThat(answers.size()).isEqualTo(1);

        Question questionFromDb = questionResource.getQuestionById(returnedQuestion.getId()).toBlocking().singleOrDefault(null);
        assertThat(questionFromDb.isAnswerAccepted()).isTrue();
    }

    @Test
    public void shouldNotNotifySlackWhenAnswererAndQuestionerIsTheSame() {
        // given user creates a question
        Auth questioner = newUser();
        when(mockedSlackResource.getUserId(questioner.getEmail())).thenReturn(just(SLACK_USER_ID));
        Question question = newQuestion();

        Question returnedQuestion = questionResource.createQuestion(questioner, question).toBlocking().singleOrDefault(null);
        assertThat(returnedQuestion).isNotNull();
        assertThat(returnedQuestion.getId()).isGreaterThan(0);

        // and someone answers the question
        Answer answer         = newAnswer();
        Answer returnedAnswer = answerResource.createAnswer(questioner, answer, returnedQuestion.getId()).toBlocking().singleOrDefault(null);
        assertThat(returnedAnswer).isNotNull();

        //Verify slack notification is send to the user who created the question
        verify(mockedSlackResource, never()).getUserId(questioner.getEmail());
        verify(mockedSlackResource, never()).postMessageToSlackAsBotUser(anyString(), anyListOf(LayoutBlock.class));
    }

    @Test
    public void shouldNotAllowToAcceptAnswerWhenQuestionIsCreatedBySomeoneElse() {
        // given a question
        Question question = newQuestion();
        Question returnedQuestion = questionResource.createQuestion(newUser(), question).toBlocking().singleOrDefault(null);
        // and an answer
        Answer answer         = newAnswer();
        // when the current user tries to accept the question, that belongs to someone else
        Answer returnedAnswer = answerResource.createAnswer(newUser(), answer, returnedQuestion.getId()).toBlocking().singleOrDefault(null);
        // then a exception shall be thrown stating that the request is invalid.
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> answerResource.markAsAcceptedAnswer(newUser(), returnedAnswer.getId()).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertThat(e.getStatus()).isEqualTo(BAD_REQUEST);
                assertThat(e.getError()).isEqualTo(ERROR_NOT_OWNER_OF_QUESTION);
            });
    }

    @Test
    public void shouldThrowInternalErrorWhenCreateAnswerQueryFails() {
        // given a question
        Question question = questionResource.createQuestion(newUser(), newQuestion()).toBlocking().singleOrDefault(null);
        // and the db malfunctions on the create
        RuntimeException dbException   = new RuntimeException();
        AnswerDao        answerDaoMock = mock(AnswerDao.class);
        when(answerDaoMock.createAnswer(anyLong(), anyLong(), any())).thenReturn(error(dbException));
        AnswerResource mockedResource = new AnswerResourceImpl(
            answerDaoMock,
            null,
            testSetup.getInjector().getInstance(DaoTransactions.class),
            mockedSlackResource,
            userResource,
            new ApplicationConfig(),
            null);

        // when the answer is going to be persisted, exception is returned
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> mockedResource.createAnswer(newUser(), newAnswer(), question.getId()).toBlocking().singleOrDefault(null))
            .withCause(dbException)
            .satisfies(e -> {
                assertThat(e.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
                assertThat(e.getError()).isEqualTo(ERROR_ANSWER_NOT_CREATED);
            });
    }

    @Test
    public void shouldReturnAnswerByIdWithCorrectVotes() {

        Question question = questionResource.createQuestion(newUser(), newQuestion()).toBlocking().singleOrDefault(null);
        Answer answer = answerResource.createAnswer(newUser(), newAnswer(), question.getId()).toBlocking().singleOrDefault(null);
        Auth user = newUser();

        assertAnswerById(answer, 0);

        addVote(user, answer, -1);
        assertAnswerById(answer, -1);

        answerVoteDao.deleteVote(user.getUserId(), answer.getId()).test().awaitTerminalEvent().assertNoErrors();
        assertAnswerById(answer, 0);

        addVote(user, answer, 1);
        assertAnswerById(answer, 1);
    }

    @Test
    public void shouldThrowNotFoundWhenNotFindingAnswerById() {

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> answerResource.getAnswerById(42).toBlocking().single())
            .satisfies(e -> {
                assertThat(e).isInstanceOf(WebException.class);
                assertThat(e).hasFieldOrPropertyWithValue("status", NOT_FOUND);
            });
    }

    @Test
    public void shouldReturnAnswerByQuestionIdWithCorrectVotes() {

        Auth user1 = newUser();
        Auth user2 = newUser();
        Auth user3 = newUser();

        Question question = questionResource.createQuestion(user1, newQuestion()).toBlocking().singleOrDefault(null);
        Answer answer1 = answerResource.createAnswer(user1, newAnswer(), question.getId()).toBlocking().singleOrDefault(null);

        assertVotesByQuestionId(user1, question, tuple(0, 0));

        addVote(user1, answer1, 1);
        assertVotesByQuestionId(user1, question, tuple(1, 1));
        assertVotesByQuestionId(user2, question, tuple(1, 0));

        addVote(user2, answer1, 1);
        assertVotesByQuestionId(user1, question, tuple(2, 1));

        addVote(user3, answer1, -1);
        assertVotesByQuestionId(user3, question, tuple(1, -1));

        Answer answer2 = answerResource.createAnswer(user1, newAnswer(), question.getId()).toBlocking().singleOrDefault(null);
        assertVotesByQuestionId(user3, question, tuple(1, -1), tuple(0, 0));

        addVote(user3, answer2, -1);
        assertVotesByQuestionId(user3, question, tuple(1, -1), tuple(-1, -1));
        assertVotesByQuestionId(user1, question, tuple(1, 1), tuple(-1, 0));
    }

    @Test
    public void shouldAddAnswerToQuestion() {

        Auth createdUserAuth = newUser();
        Question question = createQuestion(createdUserAuth);

        Answer answer = createAnswer();

        assertThat(answerResource.createAnswer(createdUserAuth, answer, question.getId()))
            .hasExactlyOne()
            .extracting(Answer::getId)
            .isNotNull();

        Answer createdAnswer = getFirstAnswer(createdUserAuth, question);
        assertFalse(createdAnswer.isAccepted());
        assertEquals("this is the body of the answer", createdAnswer.getAnswer());
        assertThat(createdAnswer.getVotes()).isZero();
        assertEquals("Test Subject", createdAnswer.getCreatedBy());
        assertThat(createdAnswer.getCreated())
            .isNotNull();
        assertThat(createdAnswer.getCreated().getZone())
            .isEqualTo(ZoneId.systemDefault());
    }

    @Test
    public void shouldBeAbleToUpdateAnswer() {

        Auth createdUserAuth = newUser();
        Question question = createQuestion(createdUserAuth);

        Answer answer = new Answer();
        answer.setAnswer("this is the body of the answer");

        assertThat(answerResource.createAnswer(createdUserAuth, answer, question.getId()))
            .hasExactlyOne();
        Answer updatedAnswerInput = new Answer();
        updatedAnswerInput.setAnswer("this is an updated body of the answer");

        assertThat(answerResource.updateAnswer(createdUserAuth, answer.getId(), updatedAnswerInput))
            .isEmpty();
        Answer updatedAnswer = getFirstAnswer(createdUserAuth, question);

        assertFalse(updatedAnswer.isAccepted());
        assertEquals("this is an updated body of the answer", updatedAnswer.getAnswer());
        assertEquals("Test Subject", updatedAnswer.getCreatedBy());
    }

    @Test
    public void shouldBeAbleToDeleteAnswer() {
        // given answers exists
        Auth auth = newUser();
        Question question = createQuestion(auth);

        Answer answer = createAnswer();
        Answer answer2 = createAnswer("answer2");

        assertThat(answerResource.createAnswer(auth, answer, question.getId()))
            .hasExactlyOne();
        assertThat(answerResource.createAnswer(auth, answer2, question.getId()))
            .hasExactlyOne();

        Answer createdAnswer = getFirstAnswer(auth, question);

        // when we delete a answer
        assertThat(answerResource.deleteAnswer(auth, createdAnswer.getId()))
            .isEmpty();

        // then only the answer we want to delete should be deleted
        assertThatList(answerResource.getAnswers(auth, question.getId()))
            .hasExactlyOne()
            .hasSize(1);
    }

    @Test
    public void shouldNotDeleteAnswerThatDoesNotExist() {
        Auth auth = newUser();
        long nonExistingAnswerId = 12;
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> answerResource.deleteAnswer(auth,  nonExistingAnswerId))
            .satisfies(e -> {
                assertEquals(NOT_FOUND, e.getStatus());
                assertEquals(ANSWER_NOT_FOUND, e.getError());
            });
    }

    @Test
    public void shouldBeAbleToFetchAnswersForGivenUserAndQuestion() {

        Auth authUserOne = newUser();
        Question question = createQuestion(authUserOne);

        Answer answer = createAnswer();

        Auth authUserTwo = newUser();

        Question questionForUserTwo = createQuestion(authUserTwo);

        Answer answerForUserTwo = createAnswer();
        assertThat(answerResource.createAnswer(authUserOne, answer, question.getId()))
            .hasExactlyOne();
        assertThat(answerResource.createAnswer(authUserTwo, answerForUserTwo, questionForUserTwo.getId()))
            .hasExactlyOne();

        Answer createdAnswer = getFirstAnswer(authUserOne, question);
        assertFalse(createdAnswer.isAccepted());
        assertEquals("this is the body of the answer", createdAnswer.getAnswer());
        assertThat(createdAnswer.getVotes()).isZero();
        assertEquals("Test Subject", createdAnswer.getCreatedBy());
    }

    @Test
    public void shouldYieldCorrectVotesWhenDownVotingAndUpVoting() {

        Auth auth1 = newUser();
        Auth auth2 = newUser();
        Auth auth3 = newUser();
        Answer answer = createQuestionAndAnswer(newUser());

        voteAndAssertSuccess(answerResource::downVoteAnswer, auth1, answer,-1);
        voteAndAssertFailure(answerResource::downVoteAnswer, auth1, answer);
        voteAndAssertSuccess(answerResource::upVoteAnswer, auth1, answer, 0);
        assertNoVote(auth1, answer);
        voteAndAssertSuccess(answerResource::upVoteAnswer, auth1, answer, 1);
        voteAndAssertFailure(answerResource::upVoteAnswer, auth1, answer);

        voteAndAssertSuccess(answerResource::upVoteAnswer, auth2, answer, 2);

        voteAndAssertSuccess(answerResource::upVoteAnswer, auth3, answer, 3);

        voteAndAssertSuccess(answerResource::downVoteAnswer, auth2, answer, 2);
        assertNoVote(auth2, answer);

        voteAndAssertSuccess(answerResource::downVoteAnswer, auth3, answer, 1);
        assertNoVote(auth2, answer);
    }

    @Test
    public void shouldThrowErrorWhenUserVotesOnOwnAnswer() {

        Auth auth = newUser();
        Answer answer = createQuestionAndAnswer(auth);

        voteAndAssertFailure(answerResource::downVoteAnswer, auth, answer);
        voteAndAssertFailure(answerResource::upVoteAnswer, auth, answer);
    }

    @Test
    public void shouldOrderAnswersByAcceptedAndVotesAndByCreated() {
        // given two users
        Auth auth = newUser();
        Auth otherUser = newUser();

        // and a questions
        Question question = createQuestion(auth);

        // and a answer
        Answer justAnAnswer = new Answer();
        justAnAnswer.setAnswer("Just An Answer");
        answerResource.createAnswer(auth, justAnAnswer, question.getId()).toBlocking().singleOrDefault(null);

        // and a answer that is accepted
        Answer answerThatIsAccepted = new Answer();
        answerThatIsAccepted.setAnswer("Accepted Answer");

        Long acceptedId = answerResource.createAnswer(auth, answerThatIsAccepted, question.getId()).toBlocking().singleOrDefault(null).getId();
        answerResource.markAsAcceptedAnswer(auth, acceptedId).toBlocking().singleOrDefault(null);

        // and a answer that has a vote
        Answer answerWithVote = new Answer();
        answerWithVote.setAnswer("Answer With Vote");
        Long upvotedId = answerResource.createAnswer(auth, answerWithVote, question.getId()).toBlocking().singleOrDefault(null).getId();
        answerResource.upVoteAnswer(otherUser, upvotedId).toBlocking().singleOrDefault(null);

        // when we ask for the answers
        List<String> answers = answerResource.getAnswers(auth, question.getId())
            .toBlocking()
            .single()
            .stream()
            .map(Answer::getAnswer)
            .collect(Collectors.toList());

        // then we shall get the answers ordered properly
        assertThat(answers).containsSequence("Accepted Answer", "Answer With Vote", "Just An Answer");

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
        assertThat(answerVoteDao.findVote(auth.getUserId(), answer.getId()))
            .isEmpty();
    }

    private Answer createQuestionAndAnswer(Auth auth) {
        Question question = createQuestion(auth);
        Answer answer = new Answer();
        answer.setAnswer("answer");
        return answerResource.createAnswer(auth, answer, question.getId()).toBlocking().singleOrDefault(null);
    }

    private Answer getFirstAnswer(Auth auth, Question question) {
        List<Answer> createdAnswers = answerResource.getAnswers(auth, question.getId()).toBlocking().single();
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
        List<Question> questions = questionResource.getQuestions(auth.getUserId(), null).toBlocking().single();
        assertEquals(1, questions.size());
        return questions.get(0);
    }


    private static Answer createAnswer() {
        return createAnswer( "this is the body of the answer");
    }

    private static Answer createAnswer( String answerBody) {
        Answer answer = new Answer();
        answer.setAnswer(answerBody);
        assertThat(answer.getCreated())
            .isNull();
        return answer;
    }

    private void addVote(Auth auth, Answer answer, int value) {
        answerVoteDao.createVote(new AnswerVote(auth.getUserId(), answer.getId(), value)).test().awaitTerminalEvent().assertNoErrors();
    }

    private void assertVotesByQuestionId(Auth auth, Question question, Tuple ... votesAndUserVote) {
        assertThat(answerResource.getAnswers(auth, question.getId()).test().awaitTerminalEvent().getOnNextEvents())
            .hasOnlyOneElementSatisfying(answers ->  {
                assertThat(answers)
                    .extracting(Answer::getVotes, Answer::getCurrentUserVote)
                    .containsExactlyInAnyOrder(votesAndUserVote);
            });
    }

    private void assertAnswerById(Answer answer, int votes) {
        assertThat(answerResource.getAnswerById(answer.getId()).test().awaitTerminalEvent().getOnNextEvents())
            .extracting(Answer::getId, Answer::getVotes)
            .containsExactly(tuple(answer.getId(), votes));
    }

    private static Auth newUser() {
        final User user = TestSetup.insertUser(userResource);

        return new MockAuth(user.getId(), user.getEmail());
    }

    private static Question newQuestion() {
        Question question = new Question();
        question.setTitle(RandomString.make());
        question.setQuestion(RandomString.make());
        return question;
    }

    private static Answer newAnswer() {
        Answer answer = new Answer();
        answer.setAnswer(RandomString.make());
        return answer;
    }
}
