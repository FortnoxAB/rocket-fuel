package impl;

import se.fortnox.reactivewizard.config.Config;

@Config("application")
public class ApplicationConfig {
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
