package impl;

import api.Answer;
import api.UserAnswerResource;
import api.auth.Auth;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.AnswerDao;
import dao.AnswerVote;
import dao.AnswerVoteDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;
import rx.functions.Func1;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static rx.Observable.error;
import static se.fortnox.reactivewizard.util.rx.RxUtils.exception;

@Singleton
public class UserAnswerResourceImpl implements UserAnswerResource {

    public static final String ANSWER_NOT_FOUND                    = "answer.not.found";
    public static final String NOT_OWNER_OF_ANSWER                 = "not.owner.of.answer";
    public static final String FAILED_TO_UPDATE_ANSWER             = "failed.to.update.answer";
    public static final String FAILED_TO_DELETE_ANSWER             = "failed.to.delete.answer";
    public static final String FAILED_TO_GET_ANSWER_FROM_DATABASE  = "failed.to.get.answer.from.database";
    public static final String FAILED_TO_GET_ANSWERS_FROM_DATABASE = "failed.to.get.answers.from.database";

    static final String INVALID_VOTE = "invalid.vote";

    private final AnswerDao     answerDao;
    private final AnswerVoteDao answerVoteDao;

    @Inject
    public UserAnswerResourceImpl(AnswerDao answerDao, AnswerVoteDao answerVoteDao) {
        this.answerDao = answerDao;
        this.answerVoteDao = answerVoteDao;
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
            .switchIfEmpty(exception(() -> new WebException(HttpResponseStatus.NOT_FOUND, ANSWER_NOT_FOUND)))
            .flatMap(storedAnswer -> {
                if (auth.getUserId() != storedAnswer.getUserId()) {
                    return error(new WebException(HttpResponseStatus.FORBIDDEN, NOT_OWNER_OF_ANSWER));
                }
                return answerDao.updateAnswer(auth.getUserId(), answerId, answer)
                    .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_UPDATE_ANSWER, throwable)));
            });
    }

    @Override
    public Observable<Void> deleteAnswer(Auth auth, long answerId) {
        return answerDao.getAnswerById(answerId)
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_ANSWER_FROM_DATABASE, throwable)))
            .switchIfEmpty(exception(() -> new WebException(HttpResponseStatus.NOT_FOUND, ANSWER_NOT_FOUND)))
            .flatMap(storedAnswer -> {
                if (auth.getUserId() != storedAnswer.getUserId()) {
                    return error(new WebException(HttpResponseStatus.FORBIDDEN, NOT_OWNER_OF_ANSWER));
                }
                return answerDao.deleteAnswer(auth.getUserId(), answerId)
                    .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_DELETE_ANSWER, throwable)));
            });
    }

    @Override
    public Observable<Void> upVoteAnswer(Auth auth, long answerId) {
        return handleVote(new AnswerVote(auth.getUserId(), answerId, 1));
    }

    @Override
    public Observable<Void> downVoteAnswer(Auth auth, long answerId) {
        return handleVote(new AnswerVote(auth.getUserId(), answerId, -1));
    }

    private Observable<Void> handleVote(AnswerVote newVote) {
        return answerDao.getAnswerById(newVote.getAnswerId())
            .flatMap(validateAnswerAndGetExistingVote(newVote))
            .flatMap(validateVoteAndRemoveIfZero(newVote))
            .switchIfEmpty(answerVoteDao.createVote(newVote).map(i -> newVote))
            .ignoreElements()
            .cast(Void.class);
    }

    private Func1<Answer, Observable<AnswerVote>> validateAnswerAndGetExistingVote(AnswerVote newVote) {
        return answer -> {
            if (answer.getUserId() == newVote.getUserId()) { // no voting for your own answer
                return error(new WebException(BAD_REQUEST, INVALID_VOTE));
            }
            return answerVoteDao.findVote(newVote.getUserId(), newVote.getAnswerId());
        };
    }

    private Func1<AnswerVote, Observable<AnswerVote>> validateVoteAndRemoveIfZero(AnswerVote newVote) {
        return existingVote -> {
            int totalVote = newVote.getValue() + existingVote.getValue();
            if (totalVote == 0) {
                return answerVoteDao.deleteVote(existingVote.getUserId(), existingVote.getAnswerId())
                    .map(i -> existingVote);
            }
            return error(new WebException(BAD_REQUEST, INVALID_VOTE));
        };
    }
}
