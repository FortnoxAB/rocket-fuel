package auth;

import se.fortnox.reactivewizard.config.Config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Config("ApplicationTokenConfig")
public class ApplicationTokenConfig {

    @NotNull
    @NotEmpty
    @Min(value=10, message = "application token must be at least 10 characters long")
    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
