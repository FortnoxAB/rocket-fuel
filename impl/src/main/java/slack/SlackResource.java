package slack;

import com.github.seratch.jslack.api.model.Message;
import rx.Observable;

import javax.ws.rs.PathParam;

public interface SlackResource {

    Observable<String> getUserEmail(@PathParam("userId") String userId);

    Observable<Void> postMessageToSlack(String channel, String message, String threadTs);

    Observable<Message> getMessageFromSlack(String channel, String mainMessageId);
}
