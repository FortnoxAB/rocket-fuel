package impl;

import api.AnswerResource;
import api.UserResource;
import api.auth.Auth;
import dao.AnswerDao;
import dao.AnswerInternal;
import dao.AnswerVoteDao;
import dao.QuestionDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.jaxrs.WebException;
import slack.SlackResource;

import java.sql.SQLException;

import static impl.AnswerResourceImpl.ANSWER_NOT_FOUND;
import static impl.AnswerResourceImpl.FAILED_TO_DELETE_ANSWER;
import static impl.AnswerResourceImpl.FAILED_TO_GET_ANSWERS_FROM_DATABASE;
import static impl.AnswerResourceImpl.FAILED_TO_GET_ANSWER_FROM_DATABASE;
import static impl.AnswerResourceImpl.FAILED_TO_UPDATE_ANSWER;
import static impl.AnswerResourceImpl.NOT_OWNER_OF_ANSWER;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static rx.Observable.empty;
import static rx.Observable.error;
import static rx.Observable.just;

public class AnswerResourceImplTest {

    private AnswerResource answerResource;
    private Auth           auth;

    @Mock
    private AnswerDao          answerDao;

    @Mock
    private AnswerVoteDao answerVoteDao;

    @Before
    public void beforeEach() {
        initMocks(this);
        ApplicationConfig applicationConfig = new ApplicationConfig();
        answerResource = new AnswerResourceImpl(answerDao, mock(QuestionDao.class), mock(DaoTransactions.class), mock(SlackResource.class), mock(UserResource.class), applicationConfig, answerVoteDao);
        auth = new Auth();
        auth.setUserId(123);

    }

    @Test
    public void shouldThrowInternalServerErrorIfGetAnswersFails() {
        when(answerDao.getAnswersWithUserVotes( auth.getUserId(), 123)).thenReturn(error(new SQLException("poff")));

        assertException(() -> answerResource.getAnswers(auth, 123).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_GET_ANSWERS_FROM_DATABASE);
    }

    @Test
    public void shouldThrowInternalServerErrorIfUpdateAnswerFails() {
        AnswerInternal answer = createAnswer();
        when(answerDao.updateAnswer(123,  123, answer)).thenReturn(error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));

        assertException(() -> answerResource.updateAnswer(auth, 123,answer).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_UPDATE_ANSWER);
    }


    @Test
    public void shouldThrowForbiddenIfAnswerIsNotCreatedByTheUpdater() {
        AnswerInternal answer = createAnswer(444);
        when(answerDao.updateAnswer(123,  123, answer)).thenReturn(error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));

        assertException(() -> answerResource.updateAnswer(auth, 123,answer).toBlocking().singleOrDefault(null),
            FORBIDDEN,
            NOT_OWNER_OF_ANSWER);
    }

    @Test
    public void shouldThrowNotFoundIfAnswerToUpdateCannotBeFound() {
        when(answerDao.getAnswerById(123)).thenReturn(empty());

        assertException(() -> answerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
            NOT_FOUND,
            ANSWER_NOT_FOUND);
    }

    @Test
    public void shouldThrowForbiddenIfAnswerIsNotCreatedByTheDeleter() {
        AnswerInternal answer = createAnswer(444);
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));

        assertException(() -> answerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
            FORBIDDEN,
            NOT_OWNER_OF_ANSWER);
    }

    @Test
    public void shouldThrowNotFoundIfAnswerToDeleteCannotBeFound() {
        when(answerDao.getAnswerById(123)).thenReturn(empty());

        assertException(() -> answerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
            NOT_FOUND,
            ANSWER_NOT_FOUND);
    }

    @Test
    public void shouldThrowInternalIfAnswerToDeleteCannotBeDeleted() {
        AnswerInternal answer = createAnswer();
        when(answerDao.deleteAnswer(123,  123)).thenReturn(error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));

        assertException(() -> answerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
            INTERNAL_SERVER_ERROR,
            FAILED_TO_DELETE_ANSWER);
    }

    @Test
    public void shouldThrowInternalIfAnswerToDeleteCannotBeFetchedFromDb() {
        when(answerDao.getAnswerById(123)).thenReturn(error(new SQLException("poff")));
        Auth auth = new Auth();
        auth.setUserId(123);

        assertException(() -> answerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null),
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
