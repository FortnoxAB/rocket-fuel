package impl;

import api.Question;
import api.QuestionResource;
import api.auth.Auth;
import dao.QuestionDao;
import dao.QuestionVoteDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;
import slack.SlackConfig;
import slack.SlackResource;

import java.sql.SQLException;

import static impl.QuestionResourceImpl.FAILED_TO_DELETE_QUESTION;
import static impl.QuestionResourceImpl.FAILED_TO_GET_QUESTIONS_FROM_DATABASE;
import static impl.QuestionResourceImpl.FAILED_TO_GET_QUESTION_FROM_DATABASE;
import static impl.QuestionResourceImpl.FAILED_TO_UPDATE_QUESTION_TO_DATABASE;
import static impl.QuestionResourceImpl.NOT_OWNER_OF_QUESTION;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static rx.Observable.empty;
import static rx.Observable.error;
import static rx.Observable.just;

public class QuestionResourceImplTest {

    private QuestionResource questionResource;
    private QuestionDao      questionDao;
    private QuestionVoteDao  questionVoteDao;
    private SlackResource    slackResource;
    private Question         question;
    private Auth             auth;

    @Before
    public void beforeEach() {
        questionDao = mock(QuestionDao.class);
        questionVoteDao = mock(QuestionVoteDao.class);
        slackResource = mock(SlackResource.class);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setBaseUrl("deployed.fuel.com");
        when(slackResource.postMessageToSlack(anyString(), any())).thenReturn(empty());
        questionResource = new QuestionResourceImpl(questionDao, questionVoteDao, slackResource, new SlackConfig(), applicationConfig);
        auth = new Auth(123);
        question = createQuestion(123);

    }

    @Test
    public void shouldReturnInternalServerErrorWhenCreateQuestionFails() {
        // given db error
        Question question = new Question();
        when(questionDao.addQuestion(123, question)).thenReturn(Observable.error(new SQLException("poff")));
        Auth auth = new Auth();
        auth.setUserId(123);

        // then we should return internal error to client
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> questionResource.createQuestion(auth, question).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(INTERNAL_SERVER_ERROR, e.getStatus());
                assertEquals("failed.to.add.question.to.database", e.getError());
            });
    }

    @Test
    public void shouldReturnInternalServerErrorWhenGetQuestionsFails() {
        when(questionDao.getQuestions(123, null)).thenReturn(Observable.error(new SQLException("poff")));

        assertException(() -> questionResource.getQuestions(123, null).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_GET_QUESTIONS_FROM_DATABASE);
    }

    @Test
    public void shouldReturnInternalServerErrorWhenUpdateQuestionFails() {
        when(questionDao.getQuestion(123)).thenReturn(just(question));
        when(questionDao.updateQuestion(123, 123, question)).thenReturn(Observable.error(new SQLException("poff")));

        assertException(() -> questionResource.updateQuestion(auth, 123, question).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_UPDATE_QUESTION_TO_DATABASE);
    }

    @Test
    public void shouldThrowInternalServerErrorIfQuestionCannotBeFetchedOnUpdate() {
        when(questionDao.getQuestion(123))
            .thenReturn(just(question))
            .thenReturn(error(new SQLException("poff")));
        when(questionDao.updateQuestion(123, 123, question)).thenReturn(just(1));

        assertException(() -> questionResource.updateQuestion(auth, 123, question).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_GET_QUESTION_FROM_DATABASE);
    }

    @Test
    public void shouldThrowInternalServerErrorIfQuestionCannotBeFetchedAfterUpdate() {
        when(questionDao.getQuestion(123)).thenReturn(Observable.error(new SQLException("poff")));

        assertException(() -> questionResource.updateQuestion(auth, 123, question).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_GET_QUESTION_FROM_DATABASE);
    }

    @Test
    public void shouldThrowForbiddenIfQuestionIsNotCreatedByTheDeleter() {
        Question question = createQuestion(444);
        when(questionDao.getQuestion(123)).thenReturn(just(question));

        assertException(() -> questionResource.deleteQuestion(auth, 123).toBlocking().singleOrDefault(null),
            FORBIDDEN,
            NOT_OWNER_OF_QUESTION);
    }

    @Test
    public void shouldThrowInternalIfQuestionToDeleteCannotBeDeleted() {

        when(questionDao.deleteQuestion(123, 123)).thenReturn(Observable.error(new SQLException("poff")));
        when(questionDao.getQuestion(123)).thenReturn(just(question));

        assertException(() -> questionResource.deleteQuestion(auth, 123).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_DELETE_QUESTION);
    }

    private static void assertException(ThrowableAssert.ThrowingCallable observable, HttpResponseStatus responseStatus, String error) {
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(observable)
            .satisfies(e -> {
                assertEquals(responseStatus, e.getStatus());
                assertEquals(error, e.getError());
            });
    }

    private Question createQuestion(long userId) {
        Question question = new Question();
        question.setUserId(userId);
        return question;
    }

}
