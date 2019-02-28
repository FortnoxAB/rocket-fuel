package impl;

import api.Question;
import api.UserQuestionResource;
import api.auth.Auth;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.QuestionDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;
import se.fortnox.reactivewizard.CollectionOptions;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;

import static rx.Observable.error;

@Singleton
public class UserQuestionResourceImpl implements UserQuestionResource {


    private final QuestionDao questionDao;

    @Inject
    public UserQuestionResourceImpl(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    @Override
    public Observable<List<Question>> getQuestions(long userId, CollectionOptions collectionOptions) {
        return this.questionDao
                .getQuestions(userId, collectionOptions).toList()
                .onErrorResumeNext(throwable ->
                    error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to get questions from database",throwable)));
    }

    @Override
    public Observable<Question> getQuestion(long userId, long questionId) {
        return this.questionDao
                .getQuestion(userId, questionId)
                .onErrorResumeNext(throwable ->
                    error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to get question from database", throwable)));
    }


    @Override
    public Observable<Void> postQuestion(Auth auth, Question question) {
        return this.questionDao
                .addQuestion(auth.getUserId(), question)
                .onErrorResumeNext(throwable ->
                    error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to add question to database", throwable)));
    }

    @Override
    public Observable<Question> updateQuestion(Auth auth, long questionId, Question question) {
        return this.questionDao.updateQuestion(auth.getUserId(), questionId, question).flatMap(num -> this.questionDao.getQuestion(auth.getUserId(), questionId))
            .onErrorResumeNext(throwable -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to update question to database",throwable)));
    }
}
