package auth;


import auth.openid.OpenIdConfiguration;
import com.google.common.annotations.VisibleForTesting;
import se.fortnox.reactivewizard.client.HttpClient;
import se.fortnox.reactivewizard.client.HttpClientConfig;
import se.fortnox.reactivewizard.jaxrs.JaxRsMeta;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

public class JwkResourceProvider implements Provider<JwkResource> {
    private final JwkResource jwkResource;

    @Inject
    public JwkResourceProvider(OpenIdConfiguration openIdConfiguration) throws URISyntaxException {
        HttpClientConfig httpClientConfig = new HttpClientConfig(openIdConfiguration.getJwksUri());
        URI uri = new URI(openIdConfiguration.getJwksUri());
        HttpClient client = getHttpClient(httpClientConfig, uri);
        jwkResource = client.create(JwkResource.class);

    }

    @Override
    public JwkResource get() {
        return jwkResource;
    }

    @VisibleForTesting
    HttpClient getHttpClient(HttpClientConfig httpClientConfig, URI uri) {
        return new HttpClientWithHardCodedPath(httpClientConfig, uri);
    }

    class HttpClientWithHardCodedPath extends HttpClient {
        private final String path;

        HttpClientWithHardCodedPath(HttpClientConfig config, URI uri) {
            super(config);
            path = uri.getPath();
        }

        @Override
        protected String getPath(Method method, Object[] arguments, JaxRsMeta meta) {
            return path;
        }
    }
}
