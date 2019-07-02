package slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.request.channels.ChannelsRepliesRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersInfoRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersLookupByEmailRequest;
import com.github.seratch.jslack.api.model.Message;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.List;
import java.util.concurrent.Callable;

import static rx.Observable.empty;
import static rx.Observable.error;
import static rx.Observable.fromCallable;
import static rx.Observable.just;

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
        return callSlack(() -> {

            final UsersInfoRequest slackRequest = UsersInfoRequest
                .builder()
                .user(userId)
                .token(slackConfig.getApiToken())
                .build();

            return slack.methods().usersInfo(slackRequest);
        }).flatMap(usersInfoResponse -> {
            if (usersInfoResponse.isOk()) {
                if (usersInfoResponse.getUser().getProfile().getEmail() == null) {
                    LOG.warn("Could not fetch users email, maybe scope users:read:email is missing: ");
                    return error(new IllegalStateException("Could not fetch user email from slack: " + usersInfoResponse.getError()));
                }
                return just(usersInfoResponse.getUser().getProfile().getDisplayName());
            } else {
                return error(new IllegalStateException("Could not fetch user email from slack: " + usersInfoResponse.getError()));
            }
        });
    }

    private <T> Observable<T> callSlack(Callable<T> callable) {
        return fromCallable(callable);
    }

    @Override
    public Observable<String> getUserId(String email) {
        return callSlack(() -> {
            final UsersLookupByEmailRequest requestByEmail = UsersLookupByEmailRequest
                .builder()
                .email(email)
                .token(slackConfig.getBotUserToken())
                .build();
            return slack.methods().usersLookupByEmail(requestByEmail);
        }).map(usersLookupByEmailResponse -> {

            if (!usersLookupByEmailResponse.isOk()) {
               throw new IllegalStateException("Could not get user from slack: " + usersLookupByEmailResponse.getError());
            }

            return usersLookupByEmailResponse.getUser().getId();
        });
    }

    @Override
    public Observable<Void> postMessageToSlack(String channel, String message, String threadId) {
        return callSlack(() -> {
            final ChatPostMessageRequest chatPostMessageRequest = ChatPostMessageRequest
                .builder()
                .channel(channel)
                .threadTs(threadId)
                .text(message)
                .token(slackConfig.getApiToken())
                .build();

            return slack.methods().chatPostMessage(chatPostMessageRequest);
        }).flatMap(chatPostMessageResponse -> {
            if (!chatPostMessageResponse.isOk()) {
                LOG.warn("Could not post to slack: {}", chatPostMessageResponse.getError());
                return error(new RuntimeException(chatPostMessageResponse.getError()));
            }
            return empty();
        });
    }

    @Override
    public Observable<Void> postMessageToSlackAsBotUser(String channel, List<LayoutBlock> message) {
        return callSlack(() -> {
            final ChatPostMessageRequest chatPostMessageRequest = ChatPostMessageRequest
                .builder()
                .channel(channel)
                .blocks(message)
                .asUser(true)
                .token(slackConfig.getBotUserToken())
                .build();
            return slack.methods().chatPostMessage(chatPostMessageRequest);
        }).flatMap(chatPostMessageResponse -> {
            if(!chatPostMessageResponse.isOk()) {
                LOG.warn("Could not post to slack: {}", chatPostMessageResponse.getError());
                return error(new RuntimeException(chatPostMessageResponse.getError()));
            }
            return empty();
        });
    }

    @Override
    public Observable<Message> getMessageFromSlack(String channel, String messageId) {
        return callSlack(() -> {
            final ChannelsRepliesRequest getMessageFromSlack = ChannelsRepliesRequest
                .builder()
                .channel(channel)
                .threadTs(messageId)
                .token(slackConfig.getApiToken())
                .build();
            return slack.methods().channelsReplies(getMessageFromSlack);
        })
            .map(channelsRepliesResponse -> {
                if (channelsRepliesResponse.isOk()) {
                   return channelsRepliesResponse.getMessages().get(0);
                }
                LOG.warn("Could not get message from slack: {}", channelsRepliesResponse.getError());
                throw new WebException(HttpResponseStatus.INTERNAL_SERVER_ERROR, channelsRepliesResponse.getError() + " " + channelsRepliesResponse.getWarning());
            });
    }
}
