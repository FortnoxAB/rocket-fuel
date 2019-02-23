package auth;


import auth.openid.OpenIdConfiguration;
import se.fortnox.reactivewizard.client.HttpClient;
import se.fortnox.reactivewizard.client.HttpClientConfig;
import se.fortnox.reactivewizard.client.HttpClientProvider;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URISyntaxException;

public class JwkResourceProvider implements Provider<JwkResource> {
    private final JwkResource jwkResource;

    @Inject
    public JwkResourceProvider(HttpClientProvider httpClientProvider, OpenIdConfiguration openIdConfiguration) throws URISyntaxException {
        HttpClientConfig httpClientConfig = new HttpClientConfig(openIdConfiguration.getJwksUri());
        HttpClient client = httpClientProvider.createClient(httpClientConfig);
        jwkResource = client.create(JwkResource.class);
    }

    @Override
    public JwkResource get() {
        return jwkResource;
    }
}
