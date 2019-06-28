package slack;

import api.User;
import api.UserResource;
import api.auth.Auth;
import com.github.seratch.jslack.api.model.Message;
import rx.Observable;

abstract class AbstractMessageHandler implements SlackMessageHandler {

    protected final SlackResource slackResource;
    protected final UserResource  userResource;

    AbstractMessageHandler(SlackResource slackResource, UserResource userResource) {
        this.slackResource = slackResource;
        this.userResource = userResource;
    }

    protected Observable<Auth> getUserId(Message mainMessage) {
        return slackResource.getUserEmail(mainMessage.getUser())
                .flatMap(email -> userResource.getUserByEmail(email, true))
                .map(User::getId)
                .map(Auth::new);
    }
}
