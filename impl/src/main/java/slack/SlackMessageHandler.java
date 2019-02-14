package slack;

import com.google.gson.JsonObject;
import rx.Observable;

/**
 * Interface to be used by message handlers handling messages from slack.
 *
 */
public interface SlackMessageHandler {

    /**
     * Returns the type this messagehandler will recieve events for
     * @return
     */
    boolean shouldHandle(JsonObject body);

    /**
     * Method that handlesTheMessage
     * Handles the message.
     * @param message
     */
    Observable<Void> handleMessage(JsonObject message);
}
