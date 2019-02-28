package slack;

import se.fortnox.reactivewizard.config.Config;

import javax.validation.constraints.NotNull;

@Config("slack")
public class SlackConfig {
    /**
     * To use when making ordinary api-requests
     */
    @NotNull
    private String apiToken;

    /**
     * To use when connecting to websocket
     */
    @NotNull
    private String botUserToken;

    /**
     * Defines if the slack integration should be enabled
     */
    @NotNull
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getBotUserToken() {
        return botUserToken;
    }

    public void setBotUserToken(String botUserToken) {
        this.botUserToken = botUserToken;
    }
}
