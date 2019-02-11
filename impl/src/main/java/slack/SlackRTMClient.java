package slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.rtm.RTMClient;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import rx.Observable;
import rx.subjects.PublishSubject;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
/**
 * Message handler connecting through websocket to slack and receives events as strings.
 */
public class SlackRTMClient {

    private       RTMClient                rtmClient;
    private final Set<SlackMessageHandler> messageHandlers;

    @Inject
    public SlackRTMClient(SlackConfig slackConfig, Set<SlackMessageHandler> rtmMessageHandlers) {

        messageHandlers = rtmMessageHandlers;

        final JsonParser jsonParser = new JsonParser();

        PublishSubject<String> messageHandler = PublishSubject.create();
        messageHandler
            .doOnNext(System.out::println)
            .map(message -> {
                System.out.println(message);
                return jsonParser.parse(message).getAsJsonObject();
            })
            .flatMap(messageAsJson -> Observable.concat(
                    messageHandlers
                        .stream()
                        .filter(slackMessageHandler -> slackMessageHandler.shouldHandle(messageAsJson))
                        .map(slackMessageHandler -> slackMessageHandler.handleMessage(messageAsJson))
                        .collect(Collectors.toList())))
            .retry()
            .subscribe();

        try {

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
        } catch (IOException | DeploymentException | IllegalStateException ignore) {
            //During tests the api-token is not available, so s
            ignore.printStackTrace();
        }
    }
}
