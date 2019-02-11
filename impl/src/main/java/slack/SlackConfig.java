package slack;

import se.fortnox.reactivewizard.config.Config;

import javax.validation.constraints.NotNull;

@Config("slack")
public class SlackConfig {
    @NotNull
    /**
     * To use when making ordinary api-requests
     */
    private String apiToken;

    @NotNull
    /**
     * To use when connecting to websocket
     */
    private String botUserToken;

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
