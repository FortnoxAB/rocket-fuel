package impl;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.UserResource;
import api.auth.Auth;
import dao.AnswerDao;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.ArrayList;
import java.util.List;

import static impl.AnswerResourceImpl.ERROR_ANSWER_NOT_CREATED;
import static impl.AnswerResourceImpl.ERROR_NOT_OWNER_OF_QUESTION;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static rx.Observable.error;

public class AnswerResourceTest {
    private static QuestionResource questionResource;
    private static AnswerResource   answerResource;

    @ClassRule
    public static  PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();
    private static TestSetup           testSetup;
    private static UserResource        userResource;

    @BeforeClass
    public static void before() {
        testSetup = new TestSetup(postgreSQLContainer);
        questionResource = testSetup.getInjector().getInstance(QuestionResource.class);
        answerResource = testSetup.getInjector().getInstance(AnswerResource.class);
        userResource = testSetup.getInjector().getInstance(UserResource.class);
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
    public void markBothQuestionAndAnswerAsAccepted() {

        Auth questioner = newUser();

        Question question = newQuestion();

        Question returnedQuestion = questionResource.postQuestion(questioner, question).toBlocking().singleOrDefault(null);
        assertThat(returnedQuestion).isNotNull();
        assertThat(returnedQuestion.getId()).isGreaterThan(0);

        Auth answerer = newUser();

        Answer answer         = newAnswer();
        Answer returnedAnswer = answerResource.answerQuestion(answerer, answer, returnedQuestion.getId()).toBlocking().singleOrDefault(null);
        assertThat(returnedAnswer).isNotNull();

        answerResource.markAsAcceptedAnswer(questioner, returnedAnswer.getId()).toBlocking().singleOrDefault(null);

        List<Answer> answers = answerResource.getAnswers(returnedQuestion.getId()).toBlocking().singleOrDefault(new ArrayList<>());
        assertThat(answers.size()).isEqualTo(1);
        assertThat(answers.get(0).isAccepted()).isTrue();

        Question questionFromDb = questionResource.getQuestionById(returnedQuestion.getId()).toBlocking().singleOrDefault(null);
        assertThat(questionFromDb.isAnswerAccepted()).isTrue();
    }

    @Test
    public void userCantAcceptAnswerToOtherUsersQuestion() {

        Question question = newQuestion();

        Question returnedQuestion = questionResource.postQuestion(newUser(), question).toBlocking().singleOrDefault(null);

        Answer answer         = newAnswer();
        Answer returnedAnswer = answerResource.answerQuestion(newUser(), answer, returnedQuestion.getId()).toBlocking().singleOrDefault(null);

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> answerResource.markAsAcceptedAnswer(newUser(), returnedAnswer.getId()).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertThat(e.getStatus()).isEqualTo(BAD_REQUEST);
                assertThat(e.getError()).isEqualTo(ERROR_NOT_OWNER_OF_QUESTION);
            });
    }

    @Test
    public void errorOnAnswerNotCreated() {

        Question question = questionResource.postQuestion(newUser(), newQuestion()).toBlocking().singleOrDefault(null);

        RuntimeException dbException   = new RuntimeException();
        AnswerDao        answerDaoMock = mock(AnswerDao.class);
        when(answerDaoMock.createAnswer(anyLong(), anyLong(), any())).thenReturn(error(dbException));

        AnswerResource mockedResource = new AnswerResourceImpl(answerDaoMock, null, testSetup.getInjector().getInstance(DaoTransactions.class));

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> mockedResource.answerQuestion(newUser(), newAnswer(), question.getId()).toBlocking().singleOrDefault(null))
            .withCause(dbException)
            .satisfies(e -> {
                assertThat(e.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
                assertThat(e.getError()).isEqualTo(ERROR_ANSWER_NOT_CREATED);
            });
    }

    private Auth newUser() {
        return new MockAuth(TestSetup.insertUser(userResource).getId());
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
