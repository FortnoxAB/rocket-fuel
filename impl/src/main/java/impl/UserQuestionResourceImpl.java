package impl;

import api.Question;
import api.UserQuestionResource;
import api.auth.Auth;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.QuestionDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static rx.Observable.error;
import static se.fortnox.reactivewizard.util.rx.RxUtils.exception;

@Singleton
public class UserQuestionResourceImpl implements UserQuestionResource {

    public static final String FAILED_TO_GET_QUESTIONS_FROM_DATABASE = "failed.to.get.questions.from.database";
    public static final String FAILED_TO_GET_QUESTION_FROM_DATABASE = "failed.to.get.question.from.database";
    public static final String FAILED_TO_UPDATE_QUESTION_TO_DATABASE = "failed.to.update.question.to.database";
    public static final String QUESTION_NOT_FOUND = "question.not.found";
    public static final String NOT_OWNER_OF_QUESTION = "not.owner.of.question";
    public static final String FAILED_TO_DELETE_QUESTION = "failed.to.delete.question";
    public static final String FAILED_TO_GET_UPDATED_QUESTION_FROM_DATABASE = "failed.to.get.updated.question.from.database";
    private final QuestionDao questionDao;

    @Inject
    public UserQuestionResourceImpl(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    @Override
    public Observable<List<Question>> getQuestions(long userId) {
        return this.questionDao
                .getQuestions(userId).toList()
                .onErrorResumeNext(throwable ->
                    error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTIONS_FROM_DATABASE,throwable)));
    }

    @Override
    public Observable<Question> getQuestion(long userId, long questionId) {
        return this.questionDao
                .getQuestion(userId, questionId)
                .onErrorResumeNext(throwable ->
                    error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable)));
    }

    @Override
    public Observable<Question> updateQuestion(Auth auth, long questionId, Question question) {
        return questionDao.getQuestion(questionId)
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable)))
            .switchIfEmpty(exception(() -> new WebException(HttpResponseStatus.NOT_FOUND, QUESTION_NOT_FOUND)))
            .flatMap(storedQuestion -> {
                if(auth.getUserId() != storedQuestion.getUserId()) {
                    return error(new WebException(HttpResponseStatus.FORBIDDEN, NOT_OWNER_OF_QUESTION));
                }
                return questionDao.updateQuestion(auth.getUserId(),  questionId, question)
                    .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_UPDATE_QUESTION_TO_DATABASE, throwable)))
                    .flatMap(ignore -> questionDao.getQuestion(questionId)
                         .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable))))
                    .switchIfEmpty(exception(() -> new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE)));
            });
    }

    @Override
    public Observable<Void> deleteQuestion(Auth auth, long questionId) {
        return questionDao.getQuestion(questionId)
            .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_QUESTION_FROM_DATABASE, throwable)))
            .switchIfEmpty(exception(() -> new WebException(HttpResponseStatus.NOT_FOUND, QUESTION_NOT_FOUND)))
            .flatMap(storedAnswer -> {
                if(auth.getUserId() != storedAnswer.getUserId()) {
                    return error(new WebException(HttpResponseStatus.FORBIDDEN, NOT_OWNER_OF_QUESTION));
                }
                return questionDao.deleteQuestion(auth.getUserId(), questionId)
                    .onErrorResumeNext(throwable -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_DELETE_QUESTION, throwable)));
            });

    }
}
