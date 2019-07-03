package slack;

import api.UserResource;
import api.auth.Auth;
import com.github.seratch.jslack.api.model.Message;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import rx.Observable;

import java.util.List;

public interface SlackResource {

    Observable<String> getUserEmail(String userId);

    Observable<String> getUserId(String email);

    Observable<Void> postMessageToSlack(String channel, String message, String threadTs);

    Observable<Void> postMessageToSlackAsBotUser(String channel, List<LayoutBlock> message);

    Observable<Message> getMessageFromSlack(String channel, String mainMessageId);

    Observable<Auth> getUserId(Message mainMessage);
}
