package slack;

import com.github.seratch.jslack.api.methods.response.channels.ChannelsRepliesResponse;
import com.github.seratch.jslack.api.methods.response.channels.UsersLookupByEmailResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.users.UsersInfoResponse;
import com.github.seratch.jslack.api.model.Message;
import com.github.seratch.jslack.api.model.User;
import org.junit.Test;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SlackResourceImplTest {

    @Test
    public void handleUserInfoResponse() {
        UsersInfoResponse usersInfoResponse = new UsersInfoResponse();
        usersInfoResponse.setOk(true);

        final User.Profile profile = new User.Profile();
        profile.setEmail("tst@a.b");

        User user = new User();
        user.setProfile(profile);

        usersInfoResponse.setUser(user);

        //Successful handling
        assertThat(SlackResourceImpl.handleUserInfoResponse(usersInfoResponse)).isEqualTo(profile.getEmail());

        //Not successful handling
        usersInfoResponse.setOk(false);
        usersInfoResponse.setError("error");
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> SlackResourceImpl.handleUserInfoResponse(usersInfoResponse))
            .satisfies(e -> assertThat(e.getMessage()).isEqualTo("Could not fetch user email from slack: error"));

        //Missing scope
        usersInfoResponse.setOk(true);
        usersInfoResponse.getUser().getProfile().setEmail(null);
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> SlackResourceImpl.handleUserInfoResponse(usersInfoResponse))
            .satisfies(e -> assertThat(e.getMessage()).isEqualTo("User email from slack is null and might be due to missing scope users:read.email"));
    }

    @Test
    public void handleUserByEmailResponse() {
        UsersLookupByEmailResponse response = new UsersLookupByEmailResponse();
        response.setOk(true);
        final User user = new User();
        user.setId(UUID.randomUUID().toString());
        response.setUser(user);

        //Successful
        assertThat(SlackResourceImpl.handleUserByEmailResponse(response)).isEqualTo(user.getId());

        //Failure
        response.setOk(false);
        response.setError("error");
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
            SlackResourceImpl.handleUserByEmailResponse(response);
        }).satisfies(e -> {
            assertThat(e.getMessage()).isEqualTo("Could not get user by email from slack: error");
        });
    }

    @Test
    public void handleChatPostMessageResponse() {
        ChatPostMessageResponse chatPostMessageResponse = new ChatPostMessageResponse();
        chatPostMessageResponse.setOk(true);

        //Successful call should return empty
        SlackResourceImpl.handleChatPostMessageResponse(chatPostMessageResponse)
            .test()
            .awaitTerminalEvent()
            .assertNoErrors()
            .assertNoValues();

        //Failure call should return IllegalStateException
        chatPostMessageResponse.setOk(false);
        SlackResourceImpl.handleChatPostMessageResponse(chatPostMessageResponse)
            .test()
            .awaitTerminalEvent()
            .assertError(IllegalStateException.class);
    }

    @Test
    public void handleGetMessageResponse() {
        ChannelsRepliesResponse channelsRepliesResponse = new ChannelsRepliesResponse();
        channelsRepliesResponse.setOk(true);
        Message message = new Message();
        message.setText(UUID.randomUUID().toString());
        channelsRepliesResponse.setMessages(singletonList(message));

        assertThat(SlackResourceImpl.handleGetMessageResponse(channelsRepliesResponse).getText()).isEqualTo(message.getText());

        channelsRepliesResponse.setOk(false);
        channelsRepliesResponse.setError("error");
        channelsRepliesResponse.setWarning("warning");

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> SlackResourceImpl.handleGetMessageResponse(channelsRepliesResponse))
            .satisfies(e -> {
                assertThat(e.getMessage()).isEqualTo("error warning");
            });
    }
}
