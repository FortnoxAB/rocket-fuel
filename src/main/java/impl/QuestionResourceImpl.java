package impl;

import api.Question;
import api.QuestionResource;
import com.google.inject.Inject;
import impl.dao.AnswerDao;
import impl.dao.QuestionDao;
import impl.dao.UserDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;
import se.fortnox.reactivewizard.CollectionOptions;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.jaxrs.WebException;


import java.util.List;

import static rx.Observable.error;

public class QuestionResourceImpl implements QuestionResource {


    private final QuestionDao questionDao;
    private final AnswerDao answerDao;
    private final DaoTransactions daoTransactions;
    private final UserDao userDao;
    @Inject
    public QuestionResourceImpl(QuestionDao questionDao, AnswerDao answerDao, DaoTransactions daoTransactions, UserDao userDao) {
        this.questionDao = questionDao;
        this.answerDao = answerDao;
        this.daoTransactions = daoTransactions;
        this.userDao = userDao;
    }

    public Observable<List<Question>> getQuestions(long userId, CollectionOptions collectionOptions) {
        return this.questionDao
                .getQuestions(collectionOptions).toList()
                .onErrorResumeNext(throwable -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to get questions from database",throwable)));
    }

    @Override
    public Observable<Void> postQuestion(long userId, Question question) {
        return this.questionDao
                .addQuestion(userId, question)
                .onErrorResumeNext(throwable -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to add question to database",throwable)));
    }

    @Override
    public Observable<Question> updateQuestion(long userId, long questionId, Question question) {
        return this.questionDao.updateQuestion(userId, questionId, question).flatMap(this.questionDao::getQuestion)
            .onErrorResumeNext(throwable -> error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "failed to update question to database",throwable)));
    }


}
