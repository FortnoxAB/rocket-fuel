package impl;

import api.Answer;
import api.AnswerResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.AnswerDao;
import rx.Observable;

@Singleton
public class AnswerResourceImpl implements AnswerResource {

    private final AnswerDao answerDao;

    @Inject
    public AnswerResourceImpl(AnswerDao answerDao) {

        this.answerDao = answerDao;
    }

    @Override
    public Observable<Answer> getAnswers(long questionId) {
        return answerDao.getAnswers(questionId);
    }

    @Override
    public Observable<Answer> getAnswerBySlackId(String slackId) {
        return answerDao.getAnswer(slackId);
    }

    @Override
    public Observable<Void> upVoteAnswer(String threadId) {
        return answerDao.upVoteAnswer(threadId);
    }

    @Override
    public Observable<Void> downVoteAnswer(String threadId) {
        return answerDao.downVoteAnswer(threadId);
    }
}
