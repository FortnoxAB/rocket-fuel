package impl;

import se.fortnox.reactivewizard.config.Config;

import javax.validation.constraints.NotNull;

/**
 * Contains all the configuration for the JWT authentication
 */
@Config("jwt")
public class JwtConfig {

    @NotNull
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
