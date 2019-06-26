package impl;

import api.Answer;
import api.AnswerResource;
import api.auth.Auth;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.AnswerDao;
import dao.QuestionDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static rx.Observable.error;
import static se.fortnox.reactivewizard.util.rx.RxUtils.exception;

@Singleton
public class AnswerResourceImpl implements AnswerResource {

    public static final String ERROR_NOT_OWNER_OF_QUESTION = "not.owner.of.question";
    public static final String ERROR_ANSWER_NOT_CREATED = "answer.not.created";

    private static final Logger LOG = LoggerFactory.getLogger(AnswerResourceImpl.class);

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

        return this.answerDao.createAnswer(auth.getUserId(), questionId, answer)
            .map(generatedKey -> {
                answer.setId(generatedKey.getKey());
                return answer;
            }).onErrorResumeNext(throwable ->
                error(new WebException(INTERNAL_SERVER_ERROR, ERROR_ANSWER_NOT_CREATED, throwable)));
    }

    @Override
    public Observable<List<Answer>> getAnswers(long questionId) {
        return answerDao.getAnswers(questionId).toList().doOnError(throwable -> {
            System.out.println(throwable.getMessage());
            LOG.error("Failed to get answers for question: " + questionId, throwable);
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
        return answerDao.getAnswerById(answerId)
            .flatMap(answer -> {
                if (answer.getQuestion().getUserId() != auth.getUserId()) {
                    return exception(() -> new WebException(BAD_REQUEST, ERROR_NOT_OWNER_OF_QUESTION));
                }
                Observable<Integer> markAnswerAsAccepted   = answerDao.markAsAccepted(answerId);
                Observable<Integer> markQuestionAsAnswered = questionDao.markAsAnswered(auth.getUserId(), answer.getQuestionId());
                return daoTransactions.executeTransaction(markAnswerAsAccepted, markQuestionAsAnswered);
            });
    }
}
