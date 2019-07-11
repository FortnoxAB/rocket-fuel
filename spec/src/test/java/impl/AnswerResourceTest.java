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
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.jaxrs.WebException;
import slack.SlackResource;

import java.util.ArrayList;
import java.util.List;

import static impl.AnswerResourceImpl.ERROR_ANSWER_NOT_CREATED;
import static impl.AnswerResourceImpl.ERROR_NOT_OWNER_OF_QUESTION;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
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
        Answer returnedAnswer = answerResource.answerQuestion(answerer, answer, returnedQuestion.getId()).toBlocking().singleOrDefault(null);
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

        ApplicationConfig applicationConfig = testSetup.getInjector().getInstance(ApplicationConfig.class);

        // and someone answers the question
        Answer answer         = newAnswer();
        Answer returnedAnswer = answerResource.answerQuestion(questioner, answer, returnedQuestion.getId()).toBlocking().singleOrDefault(null);
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
        Answer returnedAnswer = answerResource.answerQuestion(newUser(), answer, returnedQuestion.getId()).toBlocking().singleOrDefault(null);
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
        AnswerResource mockedResource = new AnswerResourceImpl(answerDaoMock, null, testSetup.getInjector().getInstance(DaoTransactions.class), mockedSlackResource, userResource, new ApplicationConfig());

        // when the answer is going to be persisted, exception is returned
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> mockedResource.answerQuestion(newUser(), newAnswer(), question.getId()).toBlocking().singleOrDefault(null))
            .withCause(dbException)
            .satisfies(e -> {
                assertThat(e.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
                assertThat(e.getError()).isEqualTo(ERROR_ANSWER_NOT_CREATED);
            });
    }

    @Test
    public void shouldReturnAnswerByIdWithCorrectVotes() {

        Question question = questionResource.createQuestion(newUser(), newQuestion()).toBlocking().singleOrDefault(null);
        Answer answer = answerResource.answerQuestion(newUser(), newAnswer(), question.getId()).toBlocking().singleOrDefault(null);
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
        Answer answer1 = answerResource.answerQuestion(user1, newAnswer(), question.getId()).toBlocking().singleOrDefault(null);

        assertVotesByQuestionId(user1, question, tuple(0, 0));

        addVote(user1, answer1, 1);
        assertVotesByQuestionId(user1, question, tuple(1, 1));
        assertVotesByQuestionId(user2, question, tuple(1, 0));

        addVote(user2, answer1, 1);
        assertVotesByQuestionId(user1, question, tuple(2, 1));

        addVote(user3, answer1, -1);
        assertVotesByQuestionId(user3, question, tuple(1, -1));

        Answer answer2 = answerResource.answerQuestion(user1, newAnswer(), question.getId()).toBlocking().singleOrDefault(null);
        assertVotesByQuestionId(user3, question, tuple(1, -1), tuple(0, 0));

        addVote(user3, answer2, -1);
        assertVotesByQuestionId(user3, question, tuple(1, -1), tuple(-1, -1));
        assertVotesByQuestionId(user1, question, tuple(1, 1), tuple(-1, 0));
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
