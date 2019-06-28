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
import dao.QuestionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
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

    private static final Logger LOG = LoggerFactory.getLogger(AnswerResourceImpl.class);

    private final AnswerDao         answerDao;
    private final QuestionDao       questionDao;
    private final DaoTransactions   daoTransactions;
    private final SlackResource     slackResource;
    private final UserResource      userResource;
    private final ApplicationConfig applicationConfig;

    @Inject
    public AnswerResourceImpl(AnswerDao answerDao,
        QuestionDao questionDao,
        DaoTransactions daoTransactions,
        SlackResource slackResource,
        UserResource userResource,
        ApplicationConfig applicationConfig
    ) {
        this.answerDao = answerDao;
        this.questionDao = questionDao;
        this.daoTransactions = daoTransactions;
        this.slackResource = slackResource;
        this.userResource = userResource;
        this.applicationConfig = applicationConfig;
    }

    @Override
    public Observable<Answer> answerQuestion(Auth auth, Answer answer, long questionId) {
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
        return questionDao.getQuestionById(questionId)
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
    public Observable<Answer> getAnswerById(long answerId) {
        return answerDao.getAnswerById(answerId)
            .cast(Answer.class)
            .switchIfEmpty(exception(() -> new WebException(NOT_FOUND)));
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
