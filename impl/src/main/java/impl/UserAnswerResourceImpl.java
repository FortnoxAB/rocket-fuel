package impl;

import api.Answer;
import api.UserAnswerResource;
import api.auth.Auth;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.AnswerDao;
import dao.QuestionDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static rx.Observable.error;

@Singleton
public class UserAnswerResourceImpl implements UserAnswerResource {

    private final DaoTransactions daoTransactions;
    private       QuestionDao     questionDao;
    private       AnswerDao       answerDao;

    @Inject
    public UserAnswerResourceImpl(QuestionDao questionDao, AnswerDao answerDao, DaoTransactions daoTransactions) {
        this.questionDao = questionDao;
        this.answerDao = answerDao;
        this.daoTransactions = daoTransactions;
    }

    @Override
    public Observable<List<Answer>> getAnswers(long userId, long questionId) {
        return answerDao.getAnswers(userId, questionId).toList()
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, "failed to get answers from database", throwable)));

    }

    @Override
    public Observable<Void> updateAnswer(Auth auth, long questionId, long answerId, Answer answer) {
        return answerDao.updateAnswer(auth.getUserId(), questionId, answerId, answer)
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, "failed to update answer", throwable)));
    }
}
