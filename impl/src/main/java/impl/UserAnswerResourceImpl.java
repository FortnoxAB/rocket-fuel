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

    public static final String ANSWER_NOT_FOUND = "answer.not.found";
    public static final String NOT_OWNER_OF_ANSWER = "not.owner.of.answer";
    public static final String FAILED_TO_UPDATE_ANSWER = "failed.to.update.answer";
    public static final String FAILED_TO_DELETE_ANSWER = "failed.to.delete.answer";
    public static final String FAILED_TO_GET_ANSWER_FROM_DATABASE = "failed.to.get.answer.from.database";
    public static final String FAILED_TO_GET_ANSWERS_FROM_DATABASE = "failed.to.get.answers.from.database";

    private AnswerDao answerDao;

    @Inject
    public UserAnswerResourceImpl( AnswerDao answerDao) {
        this.answerDao = answerDao;
    }

    @Override
    public Observable<List<Answer>> getAnswers(long userId, long questionId) {
        return answerDao.getAnswers(userId, questionId).toList()
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_ANSWERS_FROM_DATABASE, throwable)));

    }

    @Override
    public Observable<Void> updateAnswer(Auth auth, long answerId, Answer answer) {
        return answerDao.getAnswerById(answerId)
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_ANSWER_FROM_DATABASE, throwable)))
            .switchIfEmpty(error(new WebException(HttpResponseStatus.NOT_FOUND, ANSWER_NOT_FOUND)))
            .flatMap(storedAnswer -> {
            if(auth.getUserId() != storedAnswer.getUserId()) {
                return error(new WebException(HttpResponseStatus.FORBIDDEN, NOT_OWNER_OF_ANSWER));
            }
            return answerDao.updateAnswer(auth.getUserId(),  answerId, answer)
                .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_UPDATE_ANSWER, throwable)));
        });
    }

    @Override
    public Observable<Void> deleteAnswer(Auth auth, long answerId) {
        return answerDao.getAnswerById(answerId)
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_ANSWER_FROM_DATABASE, throwable)))
            .switchIfEmpty(error(new WebException(HttpResponseStatus.NOT_FOUND, ANSWER_NOT_FOUND)))
            .flatMap(storedAnswer -> {
            if(auth.getUserId() != storedAnswer.getUserId()) {
                return error(new WebException(HttpResponseStatus.FORBIDDEN, NOT_OWNER_OF_ANSWER));
            }
            return answerDao.deleteAnswer(auth.getUserId(), answerId)
                .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_DELETE_ANSWER, throwable)));
        });
    }
}
