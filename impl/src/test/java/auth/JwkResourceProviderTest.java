package auth;

import auth.openid.OpenIdConfiguration;
import org.junit.Before;
import org.junit.Test;
import se.fortnox.reactivewizard.client.HttpClientConfig;

import java.net.URI;

import static org.fest.assertions.Assertions.assertThat;


public class JwkResourceProviderTest {

    private static final String FULL_URL_PATH = "https://www.googleapis.com/oauth2/v3/certs";
    private JwkResourceProvider jwkResourceProvider;

    @Before
    public void beforeEach() throws Exception {
        OpenIdConfiguration openIdConfiguration = new OpenIdConfiguration();
        openIdConfiguration.setJwksUri(FULL_URL_PATH);
        jwkResourceProvider = new JwkResourceProvider(openIdConfiguration);
    }

    @Test
    public void shouldReturnHttpClientThatAlwaysReturnsThePathFromTheConfig() throws Exception {
        HttpClientConfig httpClientConfig = new HttpClientConfig(FULL_URL_PATH);
        JwkResourceProvider.HttpClientWithHardCodedPath httpClient = (JwkResourceProvider.HttpClientWithHardCodedPath) jwkResourceProvider.getHttpClient(httpClientConfig, new URI(FULL_URL_PATH));

        assertThat(httpClient.getPath(null, null, null)).isEqualTo("/oauth2/v3/certs");
    }

    @Test
    public void shouldAlwaysReturnSameJwkResourceInstance() {
        assertThat(jwkResourceProvider.get()).isNotNull();
        assertThat(jwkResourceProvider.get()).isSameAs(jwkResourceProvider.get());
    }
}