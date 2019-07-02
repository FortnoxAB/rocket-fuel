package slack;

import com.github.seratch.jslack.api.model.Message;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import rx.Observable;

import javax.ws.rs.PathParam;
import java.util.List;

public interface SlackResource {

    Observable<String> getUserEmail(@PathParam("userId") String userId);

    Observable<String> getUserId(@PathParam("email") String email);

    Observable<Void> postMessageToSlack(String channel, String message, String threadTs);

    Observable<Void> postMessageToSlackAsBotUser(String channel, List<LayoutBlock> message);

    Observable<Message> getMessageFromSlack(String channel, String mainMessageId);
}
