package impl;

import api.Answer;
import api.AnswerResource;
import dao.AnswerDao;
import dao.QuestionDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnswerResourceImplTest {

    private AnswerResource answerResource;
    private QuestionDao questionDao;
    private AnswerDao answerDao;
    private DaoTransactions daoTransactions;

    @Before
    public void beforeEach() {
        questionDao = mock(QuestionDao.class);
        answerDao = mock(AnswerDao.class);
        daoTransactions = mock(DaoTransactions.class);
        answerResource = new AnswerResourceImpl(questionDao, answerDao, daoTransactions);
    }

    @Test
    public void shouldThrowInternalServerErrorIfGetAnswersFails() {
        when(answerDao.getAnswers(123, 123)).thenReturn(Observable.error(new SQLException("poff")));

        try {
            answerResource.getAnswers(123, 123).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException webException) {
            assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, webException.getStatus());
            assertEquals("failed to get answers from database", webException.getError());
        }
    }

    @Test
    public void shouldThrowInternalServerErrorIfCreateAnswerFails() {
        Answer answer = new Answer();
        when(answerDao.createAnswer(123, 123, answer)).thenReturn(Observable.error(new SQLException("poff")));

        try {
            answerResource.createAnswer(123, 123, answer).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException webException) {
            assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, webException.getStatus());
            assertEquals("failed to create answer", webException.getError());
        }

    }

    @Test
    public void shouldThrowInternalServerErrorIfUpdateAnswerFails() {
        Answer answer = new Answer();
        when(answerDao.updateAnswer(123, 123, 123, answer)).thenReturn(Observable.error(new SQLException("poff")));

        try {
            answerResource.updateAnswer(123, 123, 123, answer).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException webException) {
            assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, webException.getStatus());
            assertEquals("failed to update answer", webException.getError());
        }
    }

    @Test
    public void shouldThrowInternalServerErrorIfMarkAsAnsweredFails() {
        when(daoTransactions.executeTransaction(any(), any())).thenReturn(Observable.error(new SQLException("poff")));

        try {
            answerResource.markAsAnswered(123, 123, 123).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException webException) {
            assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, webException.getStatus());
            assertEquals("failed to mark question as answered", webException.getError());
        }
    }
}