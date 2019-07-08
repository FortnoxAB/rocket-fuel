package slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.request.channels.ChannelsRepliesRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersInfoRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersLookupByEmailRequest;
import com.github.seratch.jslack.api.methods.response.channels.ChannelsRepliesResponse;
import com.github.seratch.jslack.api.methods.response.channels.UsersLookupByEmailResponse;

import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.users.UsersInfoResponse;
import rx.Observable;

/**
 * Observable slack client that wraps the com.github.seratch.jslack.Slack client.
 *
 * @see "https://github.com/seratch/jslack"
 *
 */
public interface SlackClient {

    /**
     * @see MethodsClient#usersLookupByEmail(UsersLookupByEmailRequest)
     */
    Observable<UsersLookupByEmailResponse> usersLookupByEmail(UsersLookupByEmailRequest usersInfoRequest);


    /**
     * @see MethodsClient#usersInfo(UsersInfoRequest)
     */
    Observable<UsersInfoResponse> usersInfo(UsersInfoRequest slackRequest);

    /**
     * @see MethodsClient#chatPostMessage(ChatPostMessageRequest)
     */
    Observable<ChatPostMessageResponse> chatPostMessage(ChatPostMessageRequest chatPostMessageRequest);

    /**
     * @see MethodsClient#channelsReplies(ChannelsRepliesRequest)
     */
    Observable<ChannelsRepliesResponse> channelsReplies(ChannelsRepliesRequest channelsRepliesRequest);
}
