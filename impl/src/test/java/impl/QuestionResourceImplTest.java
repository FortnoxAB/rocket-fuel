package impl;

import api.Question;
import api.QuestionResource;
import api.auth.Auth;
import dao.QuestionDao;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.sql.SQLException;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuestionResourceImplTest {

    private QuestionResource questionResource;
    private QuestionDao          questionDao;

    @Before
    public void beforeEach() {
        questionDao = mock(QuestionDao.class);
        questionResource = new QuestionResourceImpl(questionDao);
    }

    @Test
    public void shouldReturnInternalServerErrorWhenPostQuestionFails() {
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


}
