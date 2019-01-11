package impl;

import api.Answer;
import api.AnswerResource;
import com.google.inject.Inject;
import impl.dao.AnswerDao;
import impl.dao.QuestionDao;
import rx.Observable;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;

public class AnswerResourceImpl implements AnswerResource {

    private final DaoTransactions daoTransactions;
    private QuestionDao questionDao;
    private AnswerDao answerDao;

    @Inject
    public AnswerResourceImpl(QuestionDao questionDao, AnswerDao answerDao, DaoTransactions daoTransactions) {

        this.questionDao = questionDao;
        this.answerDao = answerDao;
        this.daoTransactions = daoTransactions;
    }


    @Override
    public Observable<Answer> getAnswers(long userId, long questionId) {
        return answerDao.getAnswers(userId,questionId);
    }

    @Override
    public Observable<Void> createAnswer(long userId, long questionId, Answer answer) {
        return answerDao.createAnswer(userId, questionId, answer);
    }

    @Override
    public Observable<Void> updateAnswer(long userId, long questionId, long answerId, Answer answer) {
        return answerDao.updateAnswer(userId, questionId, answerId, answer);
    }

    @Override
    public Observable<Void> markAsAnswered(long userId, long questionId, long answerId) {
        Observable<Integer> markQuestionAsAnswered = this.questionDao.markAsAnswered(questionId);
        Observable<Integer> markAnswerAsAnswered =  this.answerDao.markAsAnswered(answerId);
        this.daoTransactions.createTransaction(markQuestionAsAnswered, markAnswerAsAnswered);
        return this.daoTransactions.executeTransaction(markQuestionAsAnswered, markAnswerAsAnswered);
    }
}
