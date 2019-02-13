package slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static rx.Observable.concat;

@Singleton
/**
 * Message handler connecting through websocket to slack and receives events as strings.
 */
public class SlackRTMClient {

    private static final Logger LOG = LoggerFactory.getLogger(SlackRTMClient.class);

    private       RTMClient                rtmClient;
    private final Set<SlackMessageHandler> messageHandlers;

    @Inject
    public SlackRTMClient(SlackConfig slackConfig, Set<SlackMessageHandler> rtmMessageHandlers) throws IOException, DeploymentException {

        messageHandlers = rtmMessageHandlers;

        final JsonParser jsonParser = new JsonParser();

        PublishSubject<String> messageHandler = PublishSubject.create();

        messageHandler
            .map(message -> {
                LOG.info("Message received: {}", message);
                return jsonParser.parse(message).getAsJsonObject();
            })
            .flatMap(this::handleMessage)
            .retry()
            .subscribe();

        connectClient(slackConfig, messageHandler);
    }

    private void connectClient(SlackConfig slackConfig, PublishSubject<String> messageHandler) throws IOException, DeploymentException {

        //Create websocket connection to slack
        rtmClient = new Slack().rtm(slackConfig.getBotUserToken());

        //Close connection when system is shut down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                rtmClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        rtmClient.addMessageHandler(messageHandler::onNext);

        rtmClient.connect();
    }

    /**
     *
     * Sends the incoming message to the handlers
     *
     */
    private Observable<Void> handleMessage(JsonObject messageAsJson) {
        return concat(
                messageHandlers
                    .stream()
                    .filter(slackMessageHandler -> slackMessageHandler.shouldHandle(messageAsJson))
                    .map(slackMessageHandler -> slackMessageHandler.handleMessage(messageAsJson))
                    .collect(Collectors.toList()));
    }
}
