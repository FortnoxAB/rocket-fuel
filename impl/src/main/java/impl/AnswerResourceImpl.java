package impl;

import api.Answer;
import api.AnswerResource;
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
import java.util.Objects;

import static java.util.Arrays.asList;

@Singleton
public class AnswerResourceImpl implements AnswerResource {

    private final AnswerDao       answerDao;
    private final QuestionDao     questionDao;
    private final DaoTransactions daoTransactions;

    @Inject
    public AnswerResourceImpl(AnswerDao answerDao, QuestionDao questionDao, DaoTransactions daoTransactions) {
        this.answerDao = answerDao;
        this.questionDao = questionDao;
        this.daoTransactions = daoTransactions;
    }

    @Override
    public Observable<Answer> answerQuestion(Auth auth, Answer answer, long questionId) {
        Objects.requireNonNull(answer.getAnswer());

        //If title is missing we just extract a title from the answer
        if (answer.getTitle() == null) {
            answer.setTitle(answer.getAnswer().substring(0, Math.min(10, answer.getAnswer().length())) + "...");
        }

        return this.answerDao.createAnswer(auth.getUserId(), questionId, answer)
            .map(generatedKey -> {
                answer.setId(generatedKey.getKey());
                return answer;
            }).onErrorResumeNext(throwable ->
                Observable.error(new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, throwable)));
    }

    @Override
    public Observable<List<Answer>> getAnswers(long questionId) {
        return answerDao.getAnswers(questionId).toList().doOnError(throwable -> {
            System.out.println(throwable.getMessage());
        });
    }

    @Override
    public Observable<Answer> getAnswerBySlackId(String slackId) {
        return answerDao.getAnswer(slackId);
    }

    @Override
    public Observable<Void> upVoteAnswer(String threadId) {
        return answerDao.upVoteAnswer(threadId);
    }

    public Observable<Void> downVoteAnswer(String threadId) {
        return answerDao.downVoteAnswer(threadId);
    }

    @Override
    public Observable<Void> markAsAcceptedAnswer(Auth auth, long answerId) {

        return answerDao.getQuestionIdByAnswer(answerId)
            .flatMap(questionId -> {
                Observable<Integer> markAnswerAsAccepted = answerDao.markAsAccepted(answerId);
                Observable<Integer> markQuestionAsAccepted = questionDao.markAsAnswered(auth.getUserId(), questionId);

                List<Observable<Integer>> acceptances = asList(markAnswerAsAccepted, markQuestionAsAccepted);
                return this.daoTransactions.executeTransaction(acceptances);
            });
    }
}
