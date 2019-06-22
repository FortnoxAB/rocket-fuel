package impl;

import api.Answer;
import api.UserAnswerResource;
import api.auth.Auth;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.AnswerDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static rx.Observable.error;

@Singleton
public class UserAnswerResourceImpl implements UserAnswerResource {

    private       AnswerDao       answerDao;

    @Inject
    public UserAnswerResourceImpl( AnswerDao answerDao) {
        this.answerDao = answerDao;
    }

    @Override
    public Observable<List<Answer>> getAnswers(long userId, long questionId) {
        return answerDao.getAnswers(userId, questionId).toList()
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, "failed.to.get.answers.from.database", throwable)));

    }

    @Override
    public Observable<Void> updateAnswer(Auth auth, long answerId, Answer answer) {
        return answerDao.getAnswerById(answerId)
            .switchIfEmpty(error(new WebException(HttpResponseStatus.NOT_FOUND, "answer.not.found")))
            .flatMap(storedAnswer -> {
            if(auth.getUserId() != storedAnswer.getUserId()) {
                return error(new WebException(HttpResponseStatus.FORBIDDEN, "not.owner.of.answer"));
            }

            return answerDao.updateAnswer(auth.getUserId(),  answerId, answer)
                .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, "failed.to.update.answer", throwable)));
        });
    }

    @Override
    public Observable<Void> deleteAnswer(Auth auth, long answerId) {
        return answerDao.getAnswerById(answerId)
            .switchIfEmpty(error(new WebException(HttpResponseStatus.NOT_FOUND, "answer.not.found")))
            .flatMap(storedAnswer -> {
            if(auth.getUserId() != storedAnswer.getUserId()) {
                return error(new WebException(HttpResponseStatus.FORBIDDEN, "not.owner.of.answer"));
            }
            return answerDao.deleteAnswer(auth.getUserId(), answerId)
                .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, "failed.to.delete.answer", throwable)));
        });
    }
}
