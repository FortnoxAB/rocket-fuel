package impl;

import api.Question;
import api.UserQuestionResource;
import api.auth.Auth;
import dao.QuestionDao;
import dao.QuestionVoteDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.sql.SQLException;

import static impl.UserQuestionResourceImpl.FAILED_TO_DELETE_QUESTION;
import static impl.UserQuestionResourceImpl.FAILED_TO_GET_QUESTIONS_FROM_DATABASE;
import static impl.UserQuestionResourceImpl.FAILED_TO_GET_QUESTION_FROM_DATABASE;
import static impl.UserQuestionResourceImpl.FAILED_TO_UPDATE_QUESTION_TO_DATABASE;
import static impl.UserQuestionResourceImpl.NOT_OWNER_OF_QUESTION;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static rx.Observable.error;
import static rx.Observable.just;

public class UserQuestionResourceImplTest {

    @InjectMocks
    private UserQuestionResourceImpl userQuestionResource;
    @Mock
    private QuestionDao          questionDao;
    @Mock
    private QuestionVoteDao questionVoteDao;
    private Auth                 auth;
    private Question            question;

    @Before
    public void beforeEach() {
        initMocks(this);
        auth = new Auth();
        auth.setUserId(123);
        question = createQuestion(123);
    }

    @Test
    public void shouldReturnInternalServerErrorWhenGetQuestionsFails() {
        when(questionDao.getQuestions(123)).thenReturn(Observable.error(new SQLException("poff")));

        assertException(() -> userQuestionResource.getQuestions( 123).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_GET_QUESTIONS_FROM_DATABASE);
    }

    @Test
    public void shouldReturnInternalServerErrorWhenGetQuestionFails() {
        when(questionDao.getQuestion(123, 123)).thenReturn(Observable.error(new SQLException("poff")));

        assertException(() -> userQuestionResource.getQuestion( auth, 123).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_GET_QUESTION_FROM_DATABASE);

    }

    @Test
    public void shouldReturnInternalServerErrorWhenUpdateQuestionFails() {

        when(questionDao.getQuestion(123)).thenReturn(just(question));
        when(questionDao.updateQuestion(123, 123, question)).thenReturn(Observable.error(new SQLException("poff")));

        assertException(() -> userQuestionResource.updateQuestion(auth, 123, question).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_UPDATE_QUESTION_TO_DATABASE);
    }

    @Test
    public void shouldThrowInternalServerErrorIfQuestionCannotBeFetchedOnUpdate() {
        when(questionDao.getQuestion(123))
            .thenReturn(just(question))
            .thenReturn(error(new SQLException("poff")));
        when(questionDao.updateQuestion(123, 123, question)).thenReturn(just(1));

        assertException(() -> userQuestionResource.updateQuestion(auth, 123, question).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_GET_QUESTION_FROM_DATABASE);
    }

    @Test
    public void shouldThrowInternalServerErrorIfQuestionCannotBeFetchedAfterUpdate() {
        when(questionDao.getQuestion(123)).thenReturn(Observable.error(new SQLException("poff")));

        assertException(() -> userQuestionResource.updateQuestion(auth, 123, question).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_GET_QUESTION_FROM_DATABASE);
    }

    @Test
    public void shouldThrowForbiddenIfQuestionIsNotCreatedByTheDeleter() {
        Question question = createQuestion(444);
        when(questionDao.getQuestion(123)).thenReturn(just(question));

        assertException(() -> userQuestionResource.deleteQuestion(auth, 123).toBlocking().singleOrDefault(null),
            FORBIDDEN,
            NOT_OWNER_OF_QUESTION);
    }


    @Test
    public void shouldThrowInternalIfQuestionToDeleteCannotBeDeleted() {

        when(questionDao.deleteQuestion(123,  123)).thenReturn(Observable.error(new SQLException("poff")));
        when(questionDao.getQuestion(123)).thenReturn(just(question));

        assertException(() -> userQuestionResource.deleteQuestion(auth, 123).toBlocking().singleOrDefault(null),
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
