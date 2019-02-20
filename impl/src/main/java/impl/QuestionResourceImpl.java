package impl;

import api.Question;
import api.QuestionResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.QuestionDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

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
}
