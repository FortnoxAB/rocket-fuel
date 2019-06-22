package impl;

import api.UserAnswerResource;
import api.auth.Auth;
import dao.AnswerDao;
import dao.AnswerInternal;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.sql.SQLException;

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

    @Before
    public void beforeEach() {
        answerDao = mock(AnswerDao.class);
        userAnswerResource = new UserAnswerResourceImpl(answerDao);
    }

    @Test
    public void shouldThrowInternalServerErrorIfGetAnswersFails() {
        when(answerDao.getAnswers(123, 123)).thenReturn(Observable.error(new SQLException("poff")));

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userAnswerResource.getAnswers(123, 123).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(INTERNAL_SERVER_ERROR, e.getStatus());
                assertEquals("failed.to.get.answers.from.database", e.getError());
            });
    }

    @Test
    public void shouldThrowInternalServerErrorIfUpdateAnswerFails() {
        AnswerInternal answer = new AnswerInternal();
        answer.setUserId(123L);
        when(answerDao.updateAnswer(123,  123, answer)).thenReturn(Observable.error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));
        Auth auth = new Auth();
        auth.setUserId(123);

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userAnswerResource.updateAnswer(auth, 123, answer).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(INTERNAL_SERVER_ERROR, e.getStatus());
                assertEquals("failed.to.update.answer", e.getError());
            });
    }


    @Test
    public void shouldThrowForbiddenIfAnswerIsNotCreatedByTheUpdater() {
        AnswerInternal answer = new AnswerInternal();
        answer.setUserId(444L);
        when(answerDao.updateAnswer(123,  123, answer)).thenReturn(Observable.error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));
        Auth auth = new Auth();
        auth.setUserId(123);

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userAnswerResource.updateAnswer(auth, 123, answer).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(FORBIDDEN, e.getStatus());
                assertEquals("not.owner.of.answer", e.getError());
            });
    }

    @Test
    public void shouldThrowNotFoundIfAnswerToUpdateCannotBeFound() {
        AnswerInternal answer = new AnswerInternal();
        answer.setUserId(123L);
        when(answerDao.getAnswerById(123)).thenReturn(Observable.empty());
        Auth auth = new Auth();
        auth.setUserId(123);

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userAnswerResource.updateAnswer(auth, 123 , answer).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(NOT_FOUND, e.getStatus());
                assertEquals("answer.not.found", e.getError());
            });
    }

    @Test
    public void shouldThrowForbiddenIfAnswerIsNotCreatedByTheDeleter() {
        AnswerInternal answer = new AnswerInternal();
        answer.setUserId(444L);
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));
        Auth auth = new Auth();
        auth.setUserId(123);

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userAnswerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(FORBIDDEN, e.getStatus());
                assertEquals("not.owner.of.answer", e.getError());
            });
    }

    @Test
    public void shouldThrowNotFoundIfAnswerToDeleteCannotBeFound() {
        AnswerInternal answer = new AnswerInternal();
        answer.setUserId(123L);
        when(answerDao.getAnswerById(123)).thenReturn(Observable.empty());
        Auth auth = new Auth();
        auth.setUserId(123);

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userAnswerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(NOT_FOUND, e.getStatus());
                assertEquals("answer.not.found", e.getError());
            });
    }

    @Test
    public void shouldThrowInternalIfAnswerToDeleteCannotBeDeleted() {
        AnswerInternal answer = new AnswerInternal();
        answer.setUserId(123L);
        when(answerDao.deleteAnswer(123,  123)).thenReturn(Observable.error(new SQLException("poff")));
        when(answerDao.getAnswerById(123)).thenReturn(just(answer));
        Auth auth = new Auth();
        auth.setUserId(123);

        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userAnswerResource.deleteAnswer(auth, 123).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(INTERNAL_SERVER_ERROR, e.getStatus());
                assertEquals("failed.to.delete.answer", e.getError());
            });
    }
}
