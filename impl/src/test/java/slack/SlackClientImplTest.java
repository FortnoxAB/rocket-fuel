package slack;

import auth.openid.OpenIdValidator;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.request.channels.ChannelsRepliesRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersInfoRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersLookupByEmailRequest;
import com.github.seratch.jslack.api.methods.response.channels.ChannelsRepliesResponse;
import com.github.seratch.jslack.api.methods.response.channels.UsersLookupByEmailResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.users.UsersInfoResponse;
import org.apache.log4j.Appender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.fortnox.reactivewizard.test.LoggingMockUtil;


import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;
import static se.fortnox.reactivewizard.test.TestUtil.matches;

public class SlackClientImplTest {

    private SlackConfig slackConfig;
    private SlackClient slackClient;
    private Slack slack = mock(Slack.class);
    private MethodsClient methodsClient;
    private Appender appender;

    @Before
    public void beforeEach() throws NoSuchFieldException, IllegalAccessException {
        slackConfig = new SlackConfig();
        slackConfig.setApiToken("1234");
        methodsClient = mock(MethodsClient.class);
        when(slack.methods()).thenReturn(methodsClient);
        slackClient = new SlackClientImpl(slack);
        appender = LoggingMockUtil.createMockedLogAppender(SlackClientImpl.class);
    }

    @After
    public void afterEach() throws NoSuchFieldException, IllegalAccessException {
        LoggingMockUtil.destroyMockedAppender(appender, OpenIdValidator.class);
    }

    @Test
    public void shouldCallUsersLookupByEmail() throws Exception {
        // given a user lookup request
        UsersLookupByEmailRequest usersLookupByEmailRequest = UsersLookupByEmailRequest.builder()
            .email("email@to.lookup")
            .token(slackConfig.getApiToken())
            .build();

        // when we look up user
        UsersLookupByEmailResponse usersLookupByEmailResponse = new  UsersLookupByEmailResponse();
        when(methodsClient.usersLookupByEmail(usersLookupByEmailRequest)).thenReturn(usersLookupByEmailResponse);

        UsersLookupByEmailResponse response = slackClient.usersLookupByEmail(usersLookupByEmailRequest).toBlocking().single();
        // then the real method should be invoked
        verify(methodsClient, times(1)).usersLookupByEmail(usersLookupByEmailRequest);
        // and the token in the config should be used
        assertThat(usersLookupByEmailRequest.getToken()).isEqualTo(slackConfig.getApiToken());
        // and the expected response is returned
        assertThat(response).isEqualTo(usersLookupByEmailResponse);
    }

    @Test
    public void shouldCallChatPostMessage() throws Exception {
        // given a user lookup request
        ChatPostMessageRequest chatPostMessageRequest = ChatPostMessageRequest.builder()
            .token(slackConfig.getApiToken())
            .build();

        // when we look up user
        ChatPostMessageResponse usersLookupByEmailResponse = new ChatPostMessageResponse();
        when(methodsClient.chatPostMessage(chatPostMessageRequest)).thenReturn(usersLookupByEmailResponse);

        ChatPostMessageResponse response = slackClient.chatPostMessage(chatPostMessageRequest).toBlocking().single();
        // then the real method should be invoked
        verify(methodsClient, times(1)).chatPostMessage(chatPostMessageRequest);
        // and the token in the config should be used
        assertThat(chatPostMessageRequest.getToken()).isEqualTo(slackConfig.getApiToken());
        // and the expected response is returned
        assertThat(response).isEqualTo(usersLookupByEmailResponse);
    }


    @Test
    public void shouldCallChannelsReplies() throws Exception {
        // given a user lookup request
        ChannelsRepliesRequest chatPostMessageRequest = ChannelsRepliesRequest.builder()
            .token(slackConfig.getApiToken())
            .build();

        // when we look up user
        ChannelsRepliesResponse usersLookupByEmailResponse = new ChannelsRepliesResponse();
        when(methodsClient.channelsReplies(chatPostMessageRequest)).thenReturn(usersLookupByEmailResponse);

        ChannelsRepliesResponse response = slackClient.channelsReplies(chatPostMessageRequest).toBlocking().single();
        // then the real method should be invoked
        verify(methodsClient, times(1)).channelsReplies(chatPostMessageRequest);
        // and the token in the config should be used
        assertThat(chatPostMessageRequest.getToken()).isEqualTo(slackConfig.getApiToken());
        // and the expected response is returned
        assertThat(response).isEqualTo(usersLookupByEmailResponse);
    }

    @Test
    public void shouldCallUsersInfo() throws Exception {
        // given a user lookup request
        UsersInfoRequest chatPostMessageRequest = UsersInfoRequest.builder()
            .token(slackConfig.getApiToken())
            .build();

        // when we look up user
        UsersInfoResponse usersLookupByEmailResponse = new UsersInfoResponse();
        when(methodsClient.usersInfo(chatPostMessageRequest)).thenReturn(usersLookupByEmailResponse);

        UsersInfoResponse response = slackClient.usersInfo(chatPostMessageRequest).toBlocking().single();
        // then the real method should be invoked
        verify(methodsClient, times(1)).usersInfo(chatPostMessageRequest);
        // and the token in the config should be used
        assertThat(chatPostMessageRequest.getToken()).isEqualTo(slackConfig.getApiToken());
        // and the expected response is returned
        assertThat(response).isEqualTo(usersLookupByEmailResponse);
    }

    @Test
    public void shouldLogOnIOException() throws Exception {

        // given a user lookup request
        UsersInfoRequest chatPostMessageRequest = UsersInfoRequest.builder()
            .token(slackConfig.getApiToken())
            .build();

        // when we look up user on bad network
        when(methodsClient.usersInfo(chatPostMessageRequest)).thenThrow(new IOException("connection reset by peer"));

        // when we asks slack for the message, then a exception should be returned
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> slackClient.usersInfo(chatPostMessageRequest).toBlocking().single())
            .satisfies(e -> assertThat(e.getMessage()).isEqualTo("java.io.IOException: connection reset by peer"));

        // and we should log that io exception happened
        verify(appender).doAppend(matches(log -> {
            assertThat(log.getLevel().toString()).isEqualTo("ERROR");
            assertThat(log.getMessage().toString()).contains("Failed to communicate with slack");
        }));
    }

    @Test
    public void shouldLogOnExceptionFromSlack() throws Exception{
        // given a user lookup request
        UsersInfoRequest chatPostMessageRequest = UsersInfoRequest.builder()
            .token(slackConfig.getApiToken())
            .build();

        // when we look up user a random error happens
        when(methodsClient.usersInfo(chatPostMessageRequest)).thenThrow(new NullPointerException("random error"));

        // when we asks slack for the message, then a exception should be returned
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> slackClient.usersInfo(chatPostMessageRequest).toBlocking().single())
            .satisfies(e -> assertThat(e.getMessage()).isEqualTo("random error"));

        // and we should log that the exception happened
        verify(appender).doAppend(matches(log -> {
            assertThat(log.getLevel().toString()).isEqualTo("WARN");
            assertThat(log.getMessage().toString()).contains("Got bad response while communicating with slack");
        }));

    }
}
