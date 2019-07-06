package slack;

import com.github.seratch.jslack.api.methods.request.channels.ChannelsRepliesRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersInfoRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersLookupByEmailRequest;
import com.github.seratch.jslack.api.methods.response.channels.ChannelsRepliesResponse;
import com.github.seratch.jslack.api.methods.response.channels.UsersLookupByEmailResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.users.UsersInfoResponse;
import com.github.seratch.jslack.api.model.Message;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;

import static rx.Observable.empty;
import static rx.Observable.error;

@Singleton
public class SlackResourceImpl implements SlackResource {

    private static final Logger LOG = LoggerFactory.getLogger(SlackResourceImpl.class);
    public static final String USER_EMAIL_FROM_SLACK_IS_NULL_AND_MIGHT_BE_DUE_TO_MISSING_SCOPE_USERS_READ_EMAIL = "User email from slack is null and might be due to missing scope users:read.email";
    public static final String COULD_NOT_GET_USER_BY_EMAIL_FROM_SLACK = "Could not get user by email from slack: ";
    public static final String COULD_NOT_FETCH_USER_EMAIL_FROM_SLACK = "Could not fetch user email from slack: ";
    private final SlackConfig slackConfig;
    private final SlackClient slackClient;

    @Inject
    public SlackResourceImpl(SlackConfig slackConfig, SlackClient slackClient) {
        this.slackConfig = slackConfig;
        this.slackClient = slackClient;
    }

    static String handleUserInfoResponse(UsersInfoResponse usersInfoResponse) {
        if (usersInfoResponse.isOk()) {
            if (usersInfoResponse.getUser().getProfile().getEmail() == null) {
                LOG.warn("Could not fetch users email, maybe scope users:read.email is missing");
                throw new IllegalStateException(USER_EMAIL_FROM_SLACK_IS_NULL_AND_MIGHT_BE_DUE_TO_MISSING_SCOPE_USERS_READ_EMAIL);
            }
            return usersInfoResponse.getUser().getProfile().getEmail();
        } else {
            throw new IllegalStateException(COULD_NOT_FETCH_USER_EMAIL_FROM_SLACK + usersInfoResponse.getError());
        }
    }

    static String handleUserByEmailResponse(UsersLookupByEmailResponse usersLookupByEmailResponse) {
        if (!usersLookupByEmailResponse.isOk()) {
            throw new IllegalStateException(COULD_NOT_GET_USER_BY_EMAIL_FROM_SLACK + usersLookupByEmailResponse.getError());
        }

        return usersLookupByEmailResponse.getUser().getId();
    }

    static Observable<Void> handleChatPostMessageResponse(ChatPostMessageResponse chatPostMessageResponse) {
        if (!chatPostMessageResponse.isOk()) {
            LOG.warn("Could not post to slack: {}", chatPostMessageResponse.getError());
            return error(new IllegalStateException(chatPostMessageResponse.getError()));
        }
        return empty();
    }

    static Message handleGetMessageResponse(ChannelsRepliesResponse channelsRepliesResponse) {
        if (channelsRepliesResponse.isOk()) {
            return channelsRepliesResponse.getMessages().get(0);
        }
        LOG.warn("Could not get message from slack: {}", channelsRepliesResponse.getError());
        throw new IllegalStateException(channelsRepliesResponse.getError() + " " + channelsRepliesResponse.getWarning());
    }

    @Override
    public Observable<String> getUserEmail(String userId) {
        final UsersInfoRequest slackRequest = UsersInfoRequest
            .builder()
            .user(userId)
            .token(slackConfig.getApiToken())
            .build();
        return slackClient.usersInfo(slackRequest).map(SlackResourceImpl::handleUserInfoResponse);
    }

    @Override
    public Observable<String> getUserId(String email) {
        final UsersLookupByEmailRequest requestByEmail = UsersLookupByEmailRequest
                .builder()
                .email(email)
                .token(slackConfig.getBotUserToken())
                .build();
        return slackClient.usersLookupByEmail(requestByEmail).map(SlackResourceImpl::handleUserByEmailResponse);
    }

    @Override
    public Observable<Void> postMessageToSlack(String channel, String message, String threadId) {
        final ChatPostMessageRequest chatPostMessageRequest = ChatPostMessageRequest
            .builder()
            .channel(channel)
            .threadTs(threadId)
            .text(message)
            .token(slackConfig.getApiToken())
            .build();
        return slackClient.chatPostMessage(chatPostMessageRequest)
            .flatMap(SlackResourceImpl::handleChatPostMessageResponse);
    }

    @Override
    public Observable<Void> postMessageToSlackAsBotUser(String channel, List<LayoutBlock> message) {
        final ChatPostMessageRequest chatPostMessageRequest = ChatPostMessageRequest
                .builder()
                .channel(channel)
                .blocks(message)
                .asUser(true)
                .token(slackConfig.getBotUserToken())
                .build();
        return slackClient.chatPostMessage(chatPostMessageRequest)
            .flatMap(SlackResourceImpl::handleChatPostMessageResponse);
    }

    @Override
    public Observable<Message> getMessageFromSlack(String channel, String messageId) {
        final ChannelsRepliesRequest getMessageFromSlack = ChannelsRepliesRequest
            .builder()
            .channel(channel)
            .threadTs(messageId)
            .token(slackConfig.getApiToken())
            .build();
        return slackClient.channelsReplies(getMessageFromSlack)
            .map(SlackResourceImpl::handleGetMessageResponse);
    }
}
