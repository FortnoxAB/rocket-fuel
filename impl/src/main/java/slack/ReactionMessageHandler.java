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

import java.util.concurrent.atomic.AtomicReference;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static rx.Observable.empty;
import static rx.Observable.error;

/**
 * This class handles reactions made to answers
 * <p>
 * If you react to a message with a positive emoji it will count as a vote on that particular answer
 */
@Singleton
public class ReactionMessageHandler extends AbstractMessageHandler {
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

    @Inject
    public ReactionMessageHandler(SlackResource slackResource,
        UserResource userResource,
        QuestionResource questionResource,
        AnswerResource answerResource,
        UserAnswerResource userAnswerResource
    ) {

        super(slackResource, userResource);
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

        final AtomicReference<Boolean> upVote = getUpVoteValue(message);
        //No recognized reaction
        if (upVote.get() == null) {
            return empty();
        }

        //Reaction removed -> invert the vote
        if (REACTION_REMOVED.equals(message.get(TYPE).getAsString())) {
            upVote.set(!upVote.get());
        }

        final String threadId = getThread(message);
        return slackResource.getMessageFromSlack(getChannel(message), getThread(message))
            .flatMap(mainMessage -> getUserId(mainMessage)
                .flatMap(userId -> questionResource.getQuestionBySlackThreadId(threadId)
                    .onErrorResumeNext(throwable -> {
                        //thread id is not a question but an answer
                        if (NOT_FOUND.equals(((WebException)throwable).getStatus())) {
                            return answerResource.getAnswerBySlackId(threadId)
                                .flatMap(answer -> upVote.get() ?
                                    userAnswerResource.upVoteAnswer(userId, answer.getId()).cast(Question.class) :
                                    userAnswerResource.downVoteAnswer(userId, answer.getId()).cast(Question.class));
                        }
                        return error(throwable);
                    })
                    .flatMap(question -> upVote.get() ? questionResource.upVoteQuestion(threadId) : questionResource.downVoteQuestion(threadId))));
    }

    private static AtomicReference<Boolean> getUpVoteValue(JsonObject message) {
        AtomicReference<Boolean> upVote   = new AtomicReference<>();
        String                   reaction = message.get(REACTION).getAsString();

        if (POSITIVE_EMOJIS.contains(reaction)) {
            upVote.set(true);
        }

        if (NEGATIVE_EMOJIS.contains(reaction)) {
            upVote.set(false);
        }
        return upVote;
    }

    private static String getChannel(JsonObject message) {
        return message.get(ITEM).getAsJsonObject().get(CHANNEL).getAsString();
    }

    private static String getThread(JsonObject message) {
        return message.get(ITEM).getAsJsonObject().get(THREAD).getAsString();
    }
}
