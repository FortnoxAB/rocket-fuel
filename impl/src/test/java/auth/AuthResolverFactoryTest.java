package auth;

import api.auth.Auth;
import org.junit.Test;
import se.fortnox.reactivewizard.jaxrs.params.ParamResolver;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class AuthResolverFactoryTest {

    @Test
    public void shouldReturnSameInstanceEveryTime() {
        AuthResolver authResolver = mock(AuthResolver.class);
        AuthResolverFactory authResolverFactory = new AuthResolverFactory(authResolver);

        ParamResolver<Auth> first = authResolverFactory.createParamResolver(null);
        ParamResolver<Auth> second = authResolverFactory.createParamResolver(null);

        assertSame(first,second);
    }
}