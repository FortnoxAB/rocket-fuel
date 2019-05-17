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

    private QuestionResource QuestionResource;
    private QuestionDao          questionDao;

    @Before
    public void beforeEach() {
        questionDao = mock(QuestionDao.class);
        QuestionResource = new QuestionResourceImpl(questionDao);
    }

    @Test
    public void shouldReturnInternalServerErrorWhenPostQuestionFails() {
        Question question = new Question();
        when(questionDao.addQuestion(123, question)).thenReturn(Observable.error(new SQLException("poff")));
        Auth auth = new Auth();
        auth.setUserId(123);
        try {
            QuestionResource.postQuestion(auth, question).toBlocking().singleOrDefault(null);
            fail("expected exception");
        } catch (WebException webException) {
            assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR, webException.getStatus());
            assertEquals("failed to add question to database", webException.getError());
        }
    }

    @Test
    public void shouldReturnQuestionWhenPostQuestionSuccess() {
        Question question = new Question();
        question.setTitle("My title");
        question.setBounty(3);
        question.setQuestion("My question");
        when(questionDao.addQuestion(123, question)).thenReturn(Observable.just(question));
        Auth auth = new Auth();
        auth.setUserId(123);

        Question createdQuestion = QuestionResource.postQuestion(auth, question).toBlocking().singleOrDefault(null);
        assertEquals(question, createdQuestion);
    }
}
