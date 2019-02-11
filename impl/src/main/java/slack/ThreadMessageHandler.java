package slack;

import api.Answer;
import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.User;
import api.UserResource;
import api.auth.Auth;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static rx.Observable.concat;
import static rx.Observable.error;
import static rx.Observable.merge;
import static rx.Observable.zip;
import static se.fortnox.reactivewizard.util.rx.RxUtils.first;

@Singleton
public class ThreadMessageHandler implements SlackMessageHandler {

    private static final String           SLACK_THREAD_ID = "thread_ts";
    private final        QuestionResource questionResource;
    private final        SlackResource    slackResource;
    private final        UserResource     userResource;
    private final AnswerResource answerResource;
    private static final int              DEFAULT_BOUNTY  = 50;

    @Inject
    public ThreadMessageHandler(QuestionResource questionResource,
        SlackResource slackResource,
        UserResource userResource,
        AnswerResource answerResource
    ) {
        this.questionResource = questionResource;
        this.slackResource = slackResource;
        this.userResource = userResource;
        this.answerResource = answerResource;

    }

    @Override
    public boolean shouldHandle(JsonObject body) {
        return body.has(SLACK_THREAD_ID) && !body.has("message") && !body.has("bot_id");
    }

    @Override
    public Observable<Void> handleMessage(JsonObject message) {

        return
            questionResource
                .getQuestionBySlackThreadId(message.get(SLACK_THREAD_ID).getAsString())

                //No thread found -> create new
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof WebException) {
                        if (NOT_FOUND.equals(((WebException)throwable).getStatus())) {
                            return createQuestionAndPostToSlack(message);
                        }
                    }
                    return error(throwable);
                })
                .flatMap(question -> createAnswer(message));
    }

    private Observable<Question> createQuestionAndPostToSlack(JsonObject message) {
        return concat(createMainQuestion(message), postToSlack(message).cast(Question.class));
    }

    /**
     * Posts reply to thread in slack
     * @param message the thread
     * @return
     */
    private Observable<Void> postToSlack(JsonObject message) {
        return slackResource.postMessageToSlack(
            message.get("channel").getAsString(),
            "This looks like an interesting conversation, added it to slackoverflow",
            message.get(SLACK_THREAD_ID).getAsString())
        .ignoreElements();
    }

    /**
     * Start the thread in our internal storage
     * @param message message from slack
     *
     * @return
     */
    private Observable<Question> createMainQuestion(JsonObject message) {
        String mainMessageId = message.get(SLACK_THREAD_ID).getAsString();
        String channel       = message.get("channel").getAsString();

        return
            slackResource.getMessageFromSlack(channel, mainMessageId)
                .flatMap(mainMessage -> slackResource.getUserEmail(mainMessage.getUser())
                    .flatMap(email -> userResource.getUserByEmail(email, true))
                    .map(User::getId)
                    .flatMap(userId -> {
                        Question question = new Question();

                        question.setTitle(mainMessage.getText());
                        question.setUserId(userId);
                        question.setQuestion(mainMessage.getText());
                        question.setSlackThreadId(mainMessageId);
                        question.setBounty(DEFAULT_BOUNTY);

                        return first(questionResource.postQuestion(getAuthAs(userId), question).doOnError(throwable -> {

                        }))
                            .thenReturn(question);
                    }));
    }

    private Auth getAuthAs(Long userId) {
        Auth auth = new Auth();
        auth.setUserId(userId);
        return auth;
    }

    /**
     * Create answer to question
     * @param message message to create answer for
     *
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
                    answer.setTitle(getTitleFrom(message));
                    answer.setUserId(user.getId());

                    return answerResource.createAnswer(getAuthAs(user.getId()), question.getId(), answer);
                }
        ));
    }

    /**
     * TODO figure out how to create a title from the message
     * @param message the message from slack
     * @return the title
     */
    private String getTitleFrom(JsonObject message) {
        return message.get("text").getAsString();
    }

    private String getTextFrom(JsonObject message) {
        return message.get("text").getAsString();
    }
}