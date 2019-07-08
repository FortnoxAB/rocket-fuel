package slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.request.channels.ChannelsRepliesRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersInfoRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersLookupByEmailRequest;
import com.github.seratch.jslack.api.methods.response.channels.ChannelsRepliesResponse;
import com.github.seratch.jslack.api.methods.response.channels.UsersLookupByEmailResponse;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.users.UsersInfoResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Action1;

import java.io.IOException;
import java.util.concurrent.Callable;

import static rx.Observable.fromCallable;


@Singleton
public class SlackClientImpl  implements SlackClient {

    private static final Logger LOG = LoggerFactory.getLogger(SlackClientImpl.class);

    private Slack slack;

    @Inject
    SlackClientImpl(Slack slack) {
        this.slack = slack;
    }

    @Override
    public Observable<UsersLookupByEmailResponse> usersLookupByEmail(UsersLookupByEmailRequest usersInfoRequest) {
        return callSlack(() -> slack.methods().usersLookupByEmail(usersInfoRequest));
    }

    @Override
    public Observable<UsersInfoResponse> usersInfo(UsersInfoRequest slackRequest) {
        return callSlack(() -> slack.methods().usersInfo(slackRequest));
    }


    @Override
    public Observable<ChatPostMessageResponse> chatPostMessage(ChatPostMessageRequest chatPostMessageRequest) {
        return callSlack(() -> slack.methods().chatPostMessage(chatPostMessageRequest));
    }


    @Override
    public Observable<ChannelsRepliesResponse> channelsReplies(ChannelsRepliesRequest channelsRepliesRequest) {
        return callSlack(() -> slack.methods().channelsReplies(channelsRepliesRequest));
    }

    <T> Observable<T> callSlack(Callable<T> callable) {
        return fromCallable(callable).doOnError(logError());
    }

    private static Action1<Throwable> logError() {
        return e -> {
            if(e instanceof IOException) {
                LOG.error("Failed to communicate with slack", e);
            } else {
                LOG.warn("Got bad response while communicating with slack", e);
            }
        };
    }
}
