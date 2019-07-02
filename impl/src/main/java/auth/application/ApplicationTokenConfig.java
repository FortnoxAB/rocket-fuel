package auth.application;

import se.fortnox.reactivewizard.config.Config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Config("applicationTokenConfig")
public class ApplicationTokenConfig {

    @NotNull
    @NotEmpty
    @Min(value=10, message = "application token must be at least 10 characters long")
    private String secret;

    @NotNull
    @NotEmpty
    private String domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
