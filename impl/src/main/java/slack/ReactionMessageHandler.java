package slack;

import api.AnswerResource;
import api.Question;
import api.QuestionResource;
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
 *
 * If you react to a message with a positive emoji it will count as a vote on that particular answer
 *
 */
@Singleton
public class ReactionMessageHandler implements SlackMessageHandler {
    private static final String REACTION_ADDED = "reaction_added";
    private static final String REACTION_REMOVED = "reaction_removed";

    private static final ImmutableList    HANDLED_TYPES   = ImmutableList.of(REACTION_ADDED, REACTION_REMOVED);
    private static final ImmutableList    POSITIVE_EMOJIS = ImmutableList.of("+1");
    private static final ImmutableList    NEGATIVE_EMOJIS = ImmutableList.of("-1");
    private final        QuestionResource questionResource;
    private final        AnswerResource   answerResource;

    @Inject
    public ReactionMessageHandler(QuestionResource questionResource, AnswerResource answerResource) {

        this.questionResource = questionResource;
        this.answerResource = answerResource;
    }

    @Override
    public boolean shouldHandle(String type, JsonObject body) {
        return HANDLED_TYPES.contains(type);
    }

    @Override
    public Observable<Void> handleMessage(JsonObject message) {
        final String threadId = message.get("item").getAsJsonObject().get("ts").getAsString();

        final AtomicReference<Boolean> upVote = new AtomicReference<>();
        String reaction = message.get("reaction").getAsString();

        if(POSITIVE_EMOJIS.contains(reaction)) {
            upVote.set(true);
        }

        if(NEGATIVE_EMOJIS.contains(reaction)) {
            upVote.set(false);
        }

        //No recognized reaction
        if(upVote.get() == null) {
            return empty();
        }

        //Reaction removed -> invert the vote
        if(REACTION_REMOVED.equals(message.get("type").getAsString())) {
            upVote.set(!upVote.get());
        }

        return questionResource.getQuestionBySlackThreadId(threadId)
            .onErrorResumeNext(throwable -> {
                //thread id is not a question but an answer
                if(NOT_FOUND.equals(((WebException)throwable).getStatus())) {
                    return upVote.get() ? answerResource.upVoteAnswer(threadId).cast(Question.class) : answerResource.downVoteAnswer(threadId).cast(Question.class);
                }
                return error(throwable);
            })
            .flatMap(question -> upVote.get() ? questionResource.upVoteQuestion(threadId) : questionResource.downVoteQuestion(threadId));
    }
}
