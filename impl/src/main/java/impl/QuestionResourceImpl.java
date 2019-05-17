package impl;

import api.Question;
import api.QuestionResource;
import api.auth.Auth;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.QuestionDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import static rx.Observable.error;
import static se.fortnox.reactivewizard.util.rx.RxUtils.exception;

@Singleton
public class QuestionResourceImpl implements QuestionResource {

    private static final Logger LOG = LoggerFactory.getLogger(QuestionResourceImpl.class);
    private final QuestionDao questionDao;

    @Inject
    public QuestionResourceImpl(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    @Override
    public Observable<Void> upVoteQuestion(String threadId) {
        return questionDao.upVoteQuestion(threadId)
            .doOnError(throwable -> LOG.error("query failed", throwable));
    }

    @Override
    public Observable<Void> downVoteQuestion(String threadId) {
        return questionDao.downVoteQuestion(threadId)
            .doOnError(throwable -> LOG.error("query failed", throwable));
    }

    @Override
    public Observable<Question> getQuestionBySlackThreadId(String slackThreadId) {
        return this.questionDao.getQuestionBySlackThreadId(slackThreadId).switchIfEmpty(
            exception(() -> new WebException(HttpResponseStatus.NOT_FOUND, "not_found")));
    }

	@Override
	public Observable<Question> getQuestionById(long questionId) {
      return this.questionDao.getQuestionById(questionId).switchIfEmpty(
        exception(() -> new WebException(HttpResponseStatus.NOT_FOUND, "not_found")));
	}

    @Override
    public Observable<Question> postQuestion(Auth auth, Question question) {
        return this.questionDao
          .addQuestion(auth.getUserId(), question)
          .onErrorResumeNext(throwable ->
            error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to add question to database", throwable)));
    }
}
