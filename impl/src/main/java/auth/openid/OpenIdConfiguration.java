package auth.openid;

import se.fortnox.reactivewizard.config.Config;

import javax.validation.constraints.NotNull;

/**
 * Get issue, jwksUri from:
 * https://accounts.google.com/.well-known/risc-configuration
 * Get your apps' client ID and private key from the API console:
 * https://console.developers.google.com/apis/credentials?project=_
 */
@Config("openId")
public class OpenIdConfiguration {


    @NotNull
    private String issuer;

    @NotNull
    private String jwksUri;

    @NotNull
    private String clientId;

    @NotNull
    private String privateKey;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
