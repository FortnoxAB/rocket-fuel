package slack;

import org.junit.Test;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SlackRTMClientTest {

	@Test
	public void shouldNotEnableTheRTMClientIfConfigSaysDisabled() throws IOException, DeploymentException {

		SlackConfig slackConfig = new SlackConfig();
		slackConfig.setEnabled(false);
		Set<SlackMessageHandler> messageHandlers = new HashSet<>();
		SlackMessageHandler slackMessageHandler = mock(SlackMessageHandler.class);
		new SlackRTMClient(slackConfig, messageHandlers);

		verify(slackMessageHandler, never()).shouldHandle(any());
	}

}