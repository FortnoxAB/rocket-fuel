package impl;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.UserResource;
import api.auth.Auth;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.AnswerDao;
import dao.AnswerVote;
import dao.AnswerVoteDao;
import dao.QuestionDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;
import se.fortnox.reactivewizard.db.transactions.DaoTransactions;
import se.fortnox.reactivewizard.jaxrs.WebException;
import slack.SlackResource;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static java.util.Arrays.asList;
import static rx.Observable.empty;
import static rx.Observable.error;
import static se.fortnox.reactivewizard.util.rx.RxUtils.exception;
import static se.fortnox.reactivewizard.util.rx.RxUtils.first;

@Singleton
public class AnswerResourceImpl implements AnswerResource {

    public static final String ERROR_NOT_OWNER_OF_QUESTION = "not.owner.of.question";
    public static final String ERROR_ANSWER_NOT_CREATED    = "answer.not.created";
    public static final String ANSWER_NOT_FOUND                    = "answer.not.found";
    public static final String NOT_OWNER_OF_ANSWER                 = "not.owner.of.answer";
    public static final String FAILED_TO_UPDATE_ANSWER             = "failed.to.update.answer";
    public static final String FAILED_TO_DELETE_ANSWER             = "failed.to.delete.answer";
    public static final String FAILED_TO_GET_ANSWER_FROM_DATABASE  = "failed.to.get.answer.from.database";
    public static final String FAILED_TO_GET_ANSWERS_FROM_DATABASE = "failed.to.get.answers.from.database";
    public static final String INVALID_VOTE = "invalid.vote";

    private static final Logger LOG = LoggerFactory.getLogger(AnswerResourceImpl.class);

    private final AnswerDao         answerDao;
    private final QuestionDao       questionDao;
    private final DaoTransactions   daoTransactions;
    private final SlackResource     slackResource;
    private final UserResource      userResource;
    private final ApplicationConfig applicationConfig;
    private final AnswerVoteDao     answerVoteDao;

    @Inject
    public AnswerResourceImpl(AnswerDao answerDao,
        QuestionDao questionDao,
        DaoTransactions daoTransactions,
        SlackResource slackResource,
        UserResource userResource,
        ApplicationConfig applicationConfig,
        AnswerVoteDao answerVoteDao

    ) {
        this.answerDao = answerDao;
        this.questionDao = questionDao;
        this.daoTransactions = daoTransactions;
        this.slackResource = slackResource;
        this.userResource = userResource;
        this.applicationConfig = applicationConfig;
        this.answerVoteDao = answerVoteDao;

    }

    @Override
    public Observable<Answer> createAnswer(Auth auth, Answer answer, long questionId) {
        Objects.requireNonNull(answer.getAnswer());

        return this.answerDao.createAnswer(auth.getUserId(), questionId, answer)
            .flatMap(generatedKey -> {
                answer.setId(generatedKey.getKey());
                return first(notifyQuestionOwner(auth, answer, questionId)).thenReturn(answer);
            }).onErrorResumeNext(throwable ->
                error(new WebException(INTERNAL_SERVER_ERROR, ERROR_ANSWER_NOT_CREATED, throwable)));
    }

    /**
     * Notifies the owner of the question unless the questioner and answerer is the same user
     *
     * @param answer     the answer created
     * @param questionId the id of the question
     */
    private Observable<Void> notifyQuestionOwner(Auth auth, Answer answer, long questionId) {
        return questionDao.getQuestion(questionId)
            .flatMap(question -> {
                if (!question.getUserId().equals(auth.getUserId())) {
                    return userResource.getUserById(question.getUserId())
                        .flatMap(user -> slackResource.getUserId(user.getEmail()))
                        .filter(userId -> !userId.equals(String.valueOf(answer.getUserId()))) //Don't notify if question owner writes a response
                        .flatMap(slackUserId -> slackResource.postMessageToSlackAsBotUser(slackUserId, notificationMessage(answer, question)))
                        .onErrorResumeNext(throwable -> {
                            LOG.warn("Could not notify question owner: " + question.getUserId(), throwable);
                            return empty();
                        });
                }
                return empty();
            });
    }

    private List<LayoutBlock> notificationMessage(Answer answer, Question question) {
        return asList(SectionBlock.builder()
                .text(markdownText("Your question: *%s* got an answer:", question.getTitle()))
                .build(),
            SectionBlock.builder()
                .text(markdownText(answer.getAnswer()))
                .build(),
            SectionBlock.builder()
                .text(markdownText("Head over to %s to accept the answer", slackUrl(question.getId(), answer.getId(), applicationConfig)))
                .build());
    }

    private static MarkdownTextObject markdownText(String string, String... args) {
        return MarkdownTextObject.builder()
            .text(String.format(string, args))
            .build();
    }

    /**
     * Creates a link element to be used in slack messages
     *
     * @param questionId        the id of the question
     * @param answerId          optionally
     * @param applicationConfig the application config to get the baseurl from
     * @return a slack friendly link
     */
    public static String slackUrl(Long questionId, @Nullable Long answerId, ApplicationConfig applicationConfig) {
        String link = "<" + applicationConfig.getBaseUrl() + "/question/" + questionId;

        if (answerId != null) {
            link = link + "#answer_" + answerId;
        }

        return link + "|rocket-fuel>";
    }

    @Override
    public Observable<List<Answer>> getAnswers(Auth auth, long questionId) {
        return answerDao.getAnswersWithUserVotes(auth.getUserId(), questionId)
            .toList()
            .doOnError(throwable -> {
                LOG.error("Failed to get answers for question: " + questionId, throwable);
            })
            .onErrorResumeNext((e) -> error(new WebException(INTERNAL_SERVER_ERROR, FAILED_TO_GET_ANSWERS_FROM_DATABASE, e)));
    }

    @Override
    public Observable<Answer> getAnswerBySlackId(String slackId) {
        return answerDao.getAnswer(slackId);
    }

    @Override
    public Observable<Answer> getAnswerById(long answerId) {
        return answerDao.getAnswerById(answerId)
            .cast(Answer.class)
            .switchIfEmpty(exception(() -> new WebException(NOT_FOUND)));
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
