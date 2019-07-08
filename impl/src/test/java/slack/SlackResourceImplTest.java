package slack;

import com.github.seratch.jslack.api.methods.response.channels.ChannelsRepliesResponse;
import com.github.seratch.jslack.api.methods.response.channels.UsersLookupByEmailResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.users.UsersInfoResponse;
import com.github.seratch.jslack.api.model.Message;
import com.github.seratch.jslack.api.model.User;
import com.github.seratch.jslack.api.model.block.DividerBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static rx.Observable.just;
import static slack.SlackResourceImpl.COULD_NOT_FETCH_USER_EMAIL_FROM_SLACK;
import static slack.SlackResourceImpl.COULD_NOT_GET_USER_BY_EMAIL_FROM_SLACK;
import static slack.SlackResourceImpl.MISSING_USER_EMAIL;

public class SlackResourceImplTest {

    private SlackClient slackClient;
    private SlackResource slackResource;
    private SlackConfig slackConfig;

    @Before
    public void shouldDoStuff() {
        slackConfig = new SlackConfig();
        slackConfig.setApiToken("api-token");
        slackConfig.setBotUserToken("bot-token");
        slackClient = mock(SlackClient.class);
        slackResource = new SlackResourceImpl(slackConfig, slackClient);
    }

    @Test
    public void shouldReturnUserEmailBySlackId() {
        // given that slack has the user we are asking for
        UsersInfoResponse usersInfoResponse = createSuccessfulUserResponse();
        final String userId = usersInfoResponse.getUser().getId();
        final User.Profile profile = usersInfoResponse.getUser().getProfile();

        when(slackClient.usersInfo(any())).thenReturn(just(usersInfoResponse));
        // when we ask slack for the users mail with given id
        String foundEmail = slackResource.getUserEmail(userId).toBlocking().single();

        // then we should get the mail address
        assertThat(foundEmail).isEqualTo(profile.getEmail());
    }

    @Test
    public void shouldThrowExceptionIfUserCannotBeFetchedFromSlack() {
        // given slack yields an error that a user cannot be fetched with given id
        UsersInfoResponse usersInfoResponse = createSuccessfulUserResponse();
        usersInfoResponse.setOk(false);
        usersInfoResponse.setError("error");
        final String userId = usersInfoResponse.getUser().getId();
        when(slackClient.usersInfo(any())).thenReturn(just(usersInfoResponse));

        // when we try to get user email by slack Id
        // then we should get exception
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> slackResource.getUserEmail(userId).toBlocking().single())
            .withMessage(COULD_NOT_FETCH_USER_EMAIL_FROM_SLACK+"error");
    }

    @Test
    public void shouldThrowExceptionIfMissingScopes() {
        // given slack yields an error that a user cannot be fetched due to missing scopes
        UsersInfoResponse usersInfoResponse = createSuccessfulUserResponse();
        usersInfoResponse.setOk(true);
        usersInfoResponse.getUser().getProfile().setEmail(null);
        final String userId = usersInfoResponse.getUser().getId();
        when(slackClient.usersInfo(any())).thenReturn(just(usersInfoResponse));

        // when we try to get user email by slack Id
        // then we should get exception
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> slackResource.getUserEmail(userId).toBlocking().single())
            .withMessage(MISSING_USER_EMAIL);
    }


    @Test
    public void shouldReturnSlackUserIdByEmail() {
        // given that slack has the user we are asking for
        UsersInfoResponse usersInfoResponse = createSuccessfulUserResponse();
        final String slackId = usersInfoResponse.getUser().getId();
        final User.Profile profile = usersInfoResponse.getUser().getProfile();

        when(slackClient.usersInfo(any())).thenReturn(just(usersInfoResponse));
        // when we ask slack for the users mail with given id
        String foundEmail = slackResource.getUserEmail(slackId).toBlocking().single();

        // then we should get the mail address
        assertThat(foundEmail).isEqualTo(profile.getEmail());
    }

    @Test
    public void shouldGetUserEmailByUserId() {
        // given a successful response from slack
        UsersLookupByEmailResponse usersLookupByEmailResponse = createSuccessfulUsersLookupByEmailResponse();
        final User user = usersLookupByEmailResponse.getUser();
        when(slackClient.usersLookupByEmail(any())).thenReturn(just(usersLookupByEmailResponse));

        //when we ask slack for userId by email
        String userId = slackResource.getUserId("any.email@will.do").toBlocking().single();

        // then we should get the userId
        assertThat(userId).isEqualTo(user.getId());
    }

    @Test
    public void shouldThrowExceptionWhenFailureToFetchEmail() {
        UsersLookupByEmailResponse usersLookupByEmailResponse = createSuccessfulUsersLookupByEmailResponse();

        usersLookupByEmailResponse.setOk(false);
        usersLookupByEmailResponse.setError("error");
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
            SlackResourceImpl.handleUserByEmailResponse(usersLookupByEmailResponse);
        }).withMessage(COULD_NOT_GET_USER_BY_EMAIL_FROM_SLACK + "error");
    }

    @Test
    public void shouldReturnEmptyWhenPostingMessageToSlack() {
        // given that slack will accept our chat message
        ChatPostMessageResponse chatPostMessageResponse = new ChatPostMessageResponse();
        chatPostMessageResponse.setOk(true);
        when(slackClient.chatPostMessage(any())).thenReturn(just(chatPostMessageResponse));

        //then call should return empty
        slackResource.postMessageToSlack("channel","a message", "a threadId")
            .test()
            .awaitTerminalEvent()
            .assertNoErrors()
            .assertNoValues();
    }

    @Test
    public void shouldThrowExceptionWhenFailurePostingMessageToSlack() {
        // given that we will get a bad response from slack
        ChatPostMessageResponse chatPostMessageResponse = new ChatPostMessageResponse();
        chatPostMessageResponse.setOk(false);
        chatPostMessageResponse.setError("error");
        when(slackClient.chatPostMessage(any())).thenReturn(just(chatPostMessageResponse));

        // then call should return exception
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
            slackResource.postMessageToSlack("channel", "message","threadId").toBlocking().singleOrDefault(null);
        }).withMessage("error");
    }

    @Test
    public void shouldGetMessageByMessageId() {
        // given a successful response from slack
        ChannelsRepliesResponse channelsRepliesResponse = createSuccessfulChannelRepliesResponse();
        when(slackClient.channelsReplies(any())).thenReturn(just(channelsRepliesResponse));

        //when we ask slack for messages
        Message message = slackResource.getMessageFromSlack("channel", "messageId").toBlocking().single();

        // then we should get the message
        assertThat(message.getText()).isEqualTo(channelsRepliesResponse.getMessages().get(0).getText());
    }

    @Test
    public void shouldThrowExceptionWhenFailureToRetriveMessage() {
        ChannelsRepliesResponse channelsRepliesResponse = new ChannelsRepliesResponse();
        // given that we will get a bad response from slack
        channelsRepliesResponse.setOk(false);
        channelsRepliesResponse.setError("error");
        channelsRepliesResponse.setWarning("warning");
        when(slackClient.channelsReplies(any())).thenReturn(just(channelsRepliesResponse));
        // when we ask slack for the message, then a exception should be returned
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> slackResource.getMessageFromSlack("channel", "messageId").toBlocking().single())
            .withMessage("error warning");
    }

    @Test
    public void shouldReturnEmptyIfSuccessInPostingToSlackAsBot() {

        // given a successful response from slack
        ChatPostMessageResponse chatPostMessageResponse = new ChatPostMessageResponse();
        chatPostMessageResponse.setOk(true);
        when(slackClient.chatPostMessage(any())).thenReturn(just(chatPostMessageResponse));

        //when we ask slack for messages
        List<LayoutBlock> layoutBlocks = new ArrayList<>();
        layoutBlocks.add( new DividerBlock("123"));

        // then we should get the message and nothing should be returned
        slackResource.postMessageToSlackAsBotUser("channel", layoutBlocks)
            .test()
            .awaitTerminalEvent()
            .assertNoErrors()
            .assertNoValues();
    }


    private static ChannelsRepliesResponse createSuccessfulChannelRepliesResponse() {
        ChannelsRepliesResponse channelsRepliesResponse = new ChannelsRepliesResponse();
        channelsRepliesResponse.setOk(true);
        Message message = new Message();
        message.setText(UUID.randomUUID().toString());
        channelsRepliesResponse.setMessages(singletonList(message));
        return channelsRepliesResponse;
    }


    private static UsersInfoResponse createSuccessfulUserResponse() {
        UsersInfoResponse usersInfoResponse = new UsersInfoResponse();
        usersInfoResponse.setOk(true);

        final User.Profile profile = new User.Profile();
        profile.setEmail("tst@a.b");

        User user = new User();
        user.setProfile(profile);

        usersInfoResponse.setUser(user);
        return usersInfoResponse;
    }

    private static UsersLookupByEmailResponse createSuccessfulUsersLookupByEmailResponse() {
        UsersLookupByEmailResponse response = new UsersLookupByEmailResponse();
        response.setOk(true);
        final User user = new User();
        user.setId(UUID.randomUUID().toString());
        response.setUser(user);
        return response;
    }

}
