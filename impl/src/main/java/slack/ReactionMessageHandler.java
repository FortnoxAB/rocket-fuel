package slack;

import api.AnswerResource;
import api.Question;
import api.QuestionResource;
import api.UserAnswerResource;
import api.UserResource;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static rx.Observable.empty;
import static rx.Observable.error;

/**
 * This class handles reactions made to answers
 * <p>
 * If you react to a message with a positive emoji it will count as a vote on that particular answer
 */
@Singleton
public class ReactionMessageHandler implements SlackMessageHandler {
    static final         String REACTION_ADDED   = "reaction_added";
    private static final String REACTION_REMOVED = "reaction_removed";
    static final         String MESSAGE          = "message";
    static final         String ITEM             = "item";
    static final         String TYPE             = "type";
    static final         String REACTION         = "reaction";
    static final         String CHANNEL          = "channel";
    static final         String THREAD           = "ts";

    private static final ImmutableList      HANDLED_TYPES   = ImmutableList.of(REACTION_ADDED, REACTION_REMOVED);
    private static final ImmutableList      POSITIVE_EMOJIS = ImmutableList.of("+1");
    private static final ImmutableList      NEGATIVE_EMOJIS = ImmutableList.of("-1");
    private final        QuestionResource   questionResource;
    private final        AnswerResource     answerResource;
    private final        UserAnswerResource userAnswerResource;
    private final        SlackResource      slackResource;

    @Inject
    public ReactionMessageHandler(SlackResource slackResource,
        QuestionResource questionResource,
        AnswerResource answerResource,
        UserAnswerResource userAnswerResource
    ) {

        this.slackResource = slackResource;
        this.questionResource = questionResource;
        this.answerResource = answerResource;
        this.userAnswerResource = userAnswerResource;
    }

    @Override
    public boolean shouldHandle(String type, JsonObject body) {
        return HANDLED_TYPES.contains(type) && MESSAGE.equals(body.get(ITEM).getAsJsonObject().get(TYPE).getAsString());
    }

    @Override
    public Observable<Void> handleMessage(JsonObject message) {

        final Boolean upVote = getUpVoteValue(message);

        //No recognized reaction
        if (upVote == null) {
            return empty();
        }

        final String threadId = getThread(message);
        return slackResource.getMessageFromSlack(getChannel(message), getThread(message))
            .flatMap(mainMessage -> slackResource.getUserId(mainMessage)
                .flatMap(userId -> questionResource.getQuestionBySlackThreadId(threadId)
                    .onErrorResumeNext(throwable -> {
                        //thread id is not a question but an answer
                        if (NOT_FOUND.equals(((WebException)throwable).getStatus())) {
                            return answerResource.getAnswerBySlackId(threadId)
                                .flatMap(answer -> {
                                    if (upVote) {
                                        return userAnswerResource.upVoteAnswer(userId, answer.getId());
                                    }
                                    return userAnswerResource.downVoteAnswer(userId, answer.getId());
                                })
                                .cast(Question.class);
                        }
                        return error(throwable);
                    })
                    .cast(Question.class)
                    .flatMap(question -> {
                        if (upVote) {
                            return questionResource.upVoteQuestion(threadId);
                        }
                        return questionResource.downVoteQuestion(threadId);
                    })));
    }

    private static Boolean getUpVoteValue(JsonObject message) {

        String reaction = message.get(REACTION).getAsString();

        Boolean value = null;

        if (POSITIVE_EMOJIS.contains(reaction)) {
            value = true;
        }

        if (NEGATIVE_EMOJIS.contains(reaction)) {
            value = false;
        }

        //Reaction removed -> invert the vote
        if (value != null && REACTION_REMOVED.equals(message.get(TYPE).getAsString())) {
            value = !value;
        }

        return value;
    }

    private static String getChannel(JsonObject message) {
        return message.get(ITEM).getAsJsonObject().get(CHANNEL).getAsString();
    }

    private static String getThread(JsonObject message) {
        return message.get(ITEM).getAsJsonObject().get(THREAD).getAsString();
    }
}
