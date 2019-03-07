package slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.request.channels.ChannelsRepliesRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersInfoRequest;
import com.github.seratch.jslack.api.model.Message;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import static rx.Observable.empty;
import static rx.Observable.error;
import static rx.Observable.fromCallable;

@Singleton
public class SlackResourceImpl implements SlackResource {

    private static final Logger LOG = LoggerFactory.getLogger(SlackResourceImpl.class);
    private final Slack slack;
    private final SlackConfig slackConfig;

    @Inject
    public SlackResourceImpl(SlackConfig slackConfig) {
        slack = new Slack();
        this.slackConfig = slackConfig;
    }

    @Override
    public Observable<String> getUserEmail(String userId) {
        return fromCallable(() -> slack.methods().usersInfo(
            UsersInfoRequest
            .builder()
                .user(userId)
                .token(slackConfig.getApiToken())
            .build())
            .getUser()
            .getProfile()
            .getEmail());
    }

    @Override
    public Observable<Void> postMessageToSlack(String channnel, String message, String threadId) {
        return Observable.fromCallable(() -> slack.methods().chatPostMessage(ChatPostMessageRequest
            .builder()
            .channel(channnel)
            .threadTs(threadId)
            .text(message)
            .token(slackConfig.getApiToken())
            .build())).flatMap(chatPostMessageResponse -> {
                if(!chatPostMessageResponse.isOk()) {
                    LOG.warn("Could not post to slack: {}", chatPostMessageResponse.getError());
                    return error(new RuntimeException(chatPostMessageResponse.getError()));
                }
                return empty();
        });
    }

    @Override
    public Observable<Message> getMessageFromSlack(String channel, String messageId) {
        return Observable.fromCallable(() -> slack.methods().channelsReplies(
            ChannelsRepliesRequest
                .builder()
                .channel(channel)
                .threadTs(messageId)
                .token(slackConfig.getApiToken())
                .build()
            ))
            .map(channelsRepliesResponse -> {
                if (channelsRepliesResponse.isOk()) {
                   return channelsRepliesResponse.getMessages().get(0);
                }
                LOG.warn("Could not get message from slack: {}", channelsRepliesResponse.getError());
                throw new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, channelsRepliesResponse.getError() + " " + channelsRepliesResponse.getWarning());
            });
    }
}
