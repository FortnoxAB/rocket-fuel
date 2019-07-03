package slack;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.UserResource;
import api.auth.Auth;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import impl.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import static impl.AnswerResourceImpl.slackUrl;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static java.lang.String.format;
import static rx.Observable.defer;
import static rx.Observable.empty;
import static rx.Observable.error;
import static rx.Observable.just;
import static rx.Observable.merge;
import static rx.Observable.zip;
import static se.fortnox.reactivewizard.util.rx.RxUtils.first;

@Singleton
public class ThreadMessageHandler implements SlackMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadMessageHandler.class);

    private static final String SLACK_THREAD_ID = "thread_ts";
    private static final String CHANNEL         = "channel";
    private static final int    DEFAULT_BOUNTY  = 50;

    private final SlackResource    slackResource;
    private final UserResource     userResource;
    private final QuestionResource questionResource;
    private final AnswerResource   answerResource;
    private final ApplicationConfig applicationConfig;

    @Inject
    public ThreadMessageHandler(QuestionResource questionResource,
        SlackResource slackResource,
        UserResource userResource,
        AnswerResource answerResource,
        ApplicationConfig applicationConfig
    ) {
        this.slackResource = slackResource;
        this.userResource = userResource;
        this.questionResource = questionResource;
        this.answerResource = answerResource;
        this.applicationConfig = applicationConfig;
    }

    @Override
    public boolean shouldHandle(String type, JsonObject body) {
        return body.has(SLACK_THREAD_ID) && !body.has("message") && !body.has("bot_id");
    }

    @Override
    public Observable<Void> handleMessage(JsonObject message) {

        return
            questionResource
                .getQuestionBySlackThreadId(message.get(SLACK_THREAD_ID).getAsString())
                //No thread found -> create new
                .onErrorResumeNext(throwable -> {
                    if (isNotFoundException(throwable)) {
                        return createQuestionAndPostToSlack(message);
                    }
                    return error(throwable);
                })
                .flatMap(question -> createAnswer(message));
    }

    private static boolean isNotFoundException(Throwable throwable) {
        if (throwable instanceof WebException) {
            return NOT_FOUND.equals(((WebException)throwable).getStatus());
        }
        return false;
    }

    private Observable<Question> createQuestionAndPostToSlack(JsonObject message) {
        return createMainQuestion(message).flatMap(question -> postToSlack(message, question.getId()).cast(Question.class).concatWith(just(question)));
    }

    /**
     * Posts reply to thread in slack
     *
     * @param message the thread
     * @return
     */
    private Observable<Void> postToSlack(JsonObject message, long questionId) {
        return slackResource.postMessageToSlack(
            message.get(CHANNEL).getAsString(),
            format("This looks like an interesting conversation, added it to %s", createQuestionUrl(questionId)),
            message.get(SLACK_THREAD_ID).getAsString())
            .ignoreElements();
    }

    private String createQuestionUrl(long questionId) {
        return slackUrl(questionId, null, applicationConfig);
    }

    /**
     * Start the thread in our internal storage
     *
     * @param message message from slack
     * @return
     */
    private Observable<Question> createMainQuestion(JsonObject message) {
        String mainMessageId = getThread(message);
        String channel       = getChannel(message);

        return
            slackResource.getMessageFromSlack(channel, mainMessageId)
                .flatMap(mainMessage -> slackResource.getUserId(mainMessage)
                    .flatMap(auth -> {
                        Question question = new Question();

                        question.setTitle(mainMessage.getText());
                        question.setUserId(auth.getUserId());
                        question.setQuestion(mainMessage.getText());
                        question.setSlackId(mainMessageId);
                        question.setBounty(DEFAULT_BOUNTY);

                        return first(questionResource.createQuestion(as(auth.getUserId()), question).doOnError(throwable -> LOG.error("Could not post message to slack", throwable)))
                            .thenReturn(question);
                    }))
                .doOnError(throwable -> LOG.error("Could not create question: ", throwable))
                .switchIfEmpty(defer(() -> {
                    LOG.error("Could not create question empty");
                    return empty();
                }));
    }

    private Auth as(Long userId) {
        Auth auth = new Auth();
        auth.setUserId(userId);
        return auth;
    }

    /**
     * Create answer to question
     *
     * @param message message to create answer for
     * @return
     */
    private Observable<Void> createAnswer(JsonObject message) {
        return merge(
            zip(
                slackResource.getUserEmail(message.get("user").getAsString()).flatMap(email -> userResource.getUserByEmail(email, true)),
                questionResource.getQuestionBySlackThreadId(message.get(SLACK_THREAD_ID).getAsString()),

                (user, question) -> {
                    Answer answer = new Answer();

                    answer.setAnswer(getTextFrom(message));
                    answer.setUserId(user.getId());
                    answer.setSlackId(message.get("ts").getAsString());

                    return answerResource.answerQuestion(as(user.getId()), answer, question.getId()).ignoreElements().cast(Void.class);
                }
            ));
    }

    private String getTextFrom(JsonObject message) {
        return message.get("text").getAsString();
    }

    private static String getChannel(JsonObject message) {
        return message.get(CHANNEL).getAsString();
    }

    private static String getThread(JsonObject message) {
        return message.get(SLACK_THREAD_ID).getAsString();
    }
}
