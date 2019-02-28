package slack;

import com.google.gson.JsonObject;
import rx.Observable;

/**
 * Interface to be used by message handlers handling messages from slack.
 *
 */
public interface SlackMessageHandler {

    /**
     * Decides if this handler should handle this particular message
     * @return
     */
    boolean shouldHandle(String type, JsonObject body);

    /**
     * Method that handlesTheMessage
     * Handles the message.
     * @param message
     */
    Observable<Void> handleMessage(JsonObject message);
}
