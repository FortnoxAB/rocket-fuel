package impl;

import api.Answer;
import api.UserAnswerResource;
import api.auth.Auth;
import dao.AnswerDao;
import dao.QuestionDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.sql.SQLException;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserAnswerResourceImplTest {

    private UserAnswerResource userAnswerResource;
    private QuestionDao        questionDao;
    private AnswerDao          answerDao;
    private DaoTransactions    daoTransactions;

    @Before
    public void beforeEach() {
        questionDao = mock(QuestionDao.class);
        answerDao = mock(AnswerDao.class);
        daoTransactions = mock(DaoTransactions.class);
        userAnswerResource = new UserAnswerResourceImpl(questionDao, answerDao, daoTransactions);
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
        Answer answer = new Answer();
        when(answerDao.updateAnswer(123, 123, 123, answer)).thenReturn(Observable.error(new SQLException("poff")));
        Auth auth = new Auth();
        auth.setUserId(123);
        assertThatExceptionOfType(WebException.class)
            .isThrownBy(() -> userAnswerResource.updateAnswer(auth, 123, 123, answer).toBlocking().singleOrDefault(null))
            .satisfies(e -> {
                assertEquals(INTERNAL_SERVER_ERROR, e.getStatus());
                assertEquals("failed.to.update.answer", e.getError());
            });
    }
}
