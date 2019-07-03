package slack;

import api.User;
import api.UserResource;
import api.auth.Auth;
import com.github.seratch.jslack.api.model.Message;
import rx.Observable;

class MessageHandlerUtil {

    private MessageHandlerUtil() {
    }

    static Observable<Auth> getUserId(SlackResource slackResource, UserResource userResource, Message mainMessage) {
        return slackResource.getUserEmail(mainMessage.getUser())
            .flatMap(email -> userResource.getUserByEmail(email, true))
            .map(User::getId)
            .map(Auth::new);
    }
}
