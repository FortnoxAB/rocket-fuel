package impl;

import api.Answer;
import api.UserAnswerResource;
import api.auth.Auth;
import dao.AnswerDao;
import dao.AnswerInternal;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.sql.SQLException;

import static impl.UserAnswerResourceImpl.ANSWER_NOT_FOUND;
import static impl.UserAnswerResourceImpl.FAILED_TO_DELETE_ANSWER;
import static impl.UserAnswerResourceImpl.FAILED_TO_GET_ANSWERS_FROM_DATABASE;
import static impl.UserAnswerResourceImpl.FAILED_TO_GET_ANSWER_FROM_DATABASE;
import static impl.UserAnswerResourceImpl.FAILED_TO_UPDATE_ANSWER;
import static impl.UserAnswerResourceImpl.NOT_OWNER_OF_ANSWER;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static rx.Observable.just;

public class UserAnswerResourceImplTest {

    private UserAnswerResource userAnswerResource;
    private AnswerDao          answerDao;
    private Auth auth;
    @Before
    public void beforeEach() {
        answerDao = mock(AnswerDao.class);
        userAnswerResource = new UserAnswerResourceImpl(answerDao);
        auth = new Auth();
        auth.setUserId(123);

    }

    @Test
    public void shouldThrowInternalServerErrorIfGetAnswersFails() {
        when(answerDao.getAnswers(123, 123)).thenReturn(Observable.error(new SQLException("poff")));

        assertException(() -> userAnswerResource.getAnswers(123, 123).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_GET_ANSWERS_FROM_DATABASE);
    }

    @Test
    public void shouldThrowInternalServerErrorIfUpdateAnswerFails() {
        AnswerInternal answer = createAnswer();
        when(answerDao.updateAnswer(123,  123, answer)).thenReturn(Observable.error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));

        assertException(() -> userAnswerResource.updateAnswer(auth, 123,answer).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_UPDATE_ANSWER);
    }


    @Test
    public void shouldThrowForbiddenIfAnswerIsNotCreatedByTheUpdater() {
        AnswerInternal answer = createAnswer(444);
        when(answerDao.updateAnswer(123,  123, answer)).thenReturn(Observable.error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));

        assertException(() -> userAnswerResource.updateAnswer(auth, 123,answer).toBlocking().singleOrDefault(null),
            FORBIDDEN,
            NOT_OWNER_OF_ANSWER);
    }

    @Test
    public void shouldThrowNotFoundIfAnswerToUpdateCannotBeFound() {
        AnswerInternal answer = createAnswer();
        when(answerDao.getAnswerById(123)).thenReturn(Observable.empty());

        assertException(() -> userAnswerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
            NOT_FOUND,
            ANSWER_NOT_FOUND);
    }

    @Test
    public void shouldThrowForbiddenIfAnswerIsNotCreatedByTheDeleter() {
        AnswerInternal answer = createAnswer(444);
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));

        assertException(() -> userAnswerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
            FORBIDDEN,
            NOT_OWNER_OF_ANSWER);
    }

    @Test
    public void shouldThrowNotFoundIfAnswerToDeleteCannotBeFound() {
        when(answerDao.getAnswerById(123)).thenReturn(Observable.empty());

        assertException(() -> userAnswerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
            NOT_FOUND,
            ANSWER_NOT_FOUND);
    }

    @Test
    public void shouldThrowInternalIfAnswerToDeleteCannotBeDeleted() {
        AnswerInternal answer = createAnswer();
        when(answerDao.deleteAnswer(123,  123)).thenReturn(Observable.error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));

        assertException(() -> userAnswerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_DELETE_ANSWER);
    }

    @Test
    public void shouldThrowInternalIfAnswerToDeleteCannotBeFetchedFromDb() {
        Answer answer = createAnswer();
        when(answerDao.getAnswerById(123)).thenReturn(Observable.error(new SQLException("poff")));
        Auth auth = new Auth();
        auth.setUserId(123);

        assertException(() -> userAnswerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_GET_ANSWER_FROM_DATABASE);
    }

    private static void assertException(ThrowableAssert.ThrowingCallable observable, HttpResponseStatus responseStatus, String error) {
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(observable)
            .satisfies(e -> {
                assertEquals(responseStatus, e.getStatus());
                assertEquals(error, e.getError());
            });
    }

    private static AnswerInternal createAnswer(long userId) {
        AnswerInternal answer = new AnswerInternal();
        answer.setUserId(userId);
        return answer;
    }

    private static AnswerInternal createAnswer() {
        return createAnswer(123);
    }

}
