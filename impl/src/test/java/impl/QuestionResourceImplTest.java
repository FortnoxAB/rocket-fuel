package impl;

import api.Question;
import api.QuestionResource;
import api.auth.Auth;
import dao.QuestionDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuestionResourceImplTest {

    private QuestionResource questionResource;
    private QuestionDao questionDao;

    @Before
    public void beforeEach() {
        questionDao = mock(QuestionDao.class);
        questionResource = new QuestionResourceImpl(questionDao);
    }

    @Test
    public void shouldReturnInternalServerErrorWhenGetQuestionsFails() {
        when(questionDao.getQuestions(123, null)).thenReturn(Observable.error(new SQLException("poff")));

        try {
            questionResource.getQuestions(123, null).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException webException) {
            assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, webException.getStatus());
            assertEquals("failed to get questions from database", webException.getError());
        }
    }

    @Test
    public void shouldReturnInternalServerErrorWhenGetQuestionFails() {
        when(questionDao.getQuestion(123, 123)).thenReturn(Observable.error(new SQLException("poff")));

        try {
            questionResource.getQuestion(123, 123).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException webException) {
            assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, webException.getStatus());
            assertEquals("failed to get question from database", webException.getError());
        }
    }

    @Test
    public void shouldReturnInternalServerErrorWhenPostQuestionFails() {
        Question question = new Question();
        when(questionDao.addQuestion(123, question)).thenReturn(Observable.error(new SQLException("poff")));
        Auth auth = new Auth();
        auth.setUserId(123);
        try {
            questionResource.postQuestion(auth, question).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException webException) {
            assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, webException.getStatus());
            assertEquals("failed to add question to database", webException.getError());
        }
    }


    @Test
    public void shouldReturnInternalServerErrorWhenUpdateQuestionFails() {
        Question question = new Question();
        when(questionDao.updateQuestion(123, 123, question)).thenReturn(Observable.error(new SQLException("poff")));
        Auth auth = new Auth();
        auth.setUserId(123);
        try {
            questionResource.updateQuestion(auth, 123, question).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException webException) {
            assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, webException.getStatus());
            assertEquals("failed to update question to database", webException.getError());
        }
    }
}