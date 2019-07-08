package impl;

import api.UserAnswerResource;
import api.auth.Auth;
import dao.AnswerDao;
import dao.AnswerInternal;
import dao.VoteDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
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
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static rx.Observable.empty;
import static rx.Observable.error;
import static rx.Observable.just;

public class UserAnswerResourceImplTest {

    private UserAnswerResource userAnswerResource;
    private Auth auth;

    @Mock
    private AnswerDao          answerDao;

    @Mock
    private VoteDao            voteDao;

    @Before
    public void beforeEach() {
        initMocks(this);
        userAnswerResource = new UserAnswerResourceImpl(answerDao, voteDao);
        auth = new Auth();
        auth.setUserId(123);

    }

    @Test
    public void shouldThrowInternalServerErrorIfGetAnswersFails() {
        when(answerDao.getAnswers(123, 123)).thenReturn(error(new SQLException("poff")));

        assertException(() -> userAnswerResource.getAnswers(123, 123).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_GET_ANSWERS_FROM_DATABASE);
    }

    @Test
    public void shouldThrowInternalServerErrorIfUpdateAnswerFails() {
        AnswerInternal answer = createAnswer();
        when(answerDao.updateAnswer(123,  123, answer)).thenReturn(error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));

        assertException(() -> userAnswerResource.updateAnswer(auth, 123,answer).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_UPDATE_ANSWER);
    }


    @Test
    public void shouldThrowForbiddenIfAnswerIsNotCreatedByTheUpdater() {
        AnswerInternal answer = createAnswer(444);
        when(answerDao.updateAnswer(123,  123, answer)).thenReturn(error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));

        assertException(() -> userAnswerResource.updateAnswer(auth, 123,answer).toBlocking().singleOrDefault(null),
            FORBIDDEN,
            NOT_OWNER_OF_ANSWER);
    }

    @Test
    public void shouldThrowNotFoundIfAnswerToUpdateCannotBeFound() {
        when(answerDao.getAnswerById(123)).thenReturn(empty());

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
        when(answerDao.getAnswerById(123)).thenReturn(empty());

        assertException(() -> userAnswerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
            NOT_FOUND,
            ANSWER_NOT_FOUND);
    }

    @Test
    public void shouldThrowInternalIfAnswerToDeleteCannotBeDeleted() {
        AnswerInternal answer = createAnswer();
        when(answerDao.deleteAnswer(123,  123)).thenReturn(error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));

        assertException(() -> userAnswerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_DELETE_ANSWER);
    }

    @Test
    public void shouldThrowInternalIfAnswerToDeleteCannotBeFetchedFromDb() {
        when(answerDao.getAnswerById(123)).thenReturn(error(new SQLException("poff")));
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
