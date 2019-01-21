package impl;

import api.Auth;
import com.google.inject.Inject;
import se.fortnox.reactivewizard.jaxrs.params.ParamResolver;
import se.fortnox.reactivewizard.jaxrs.params.ParamResolverFactory;

import java.lang.reflect.Parameter;

/**
 * Creates {@link AuthResolver} to be used. Selection of witch authResolver to use can be done here so that
 * we can have more precise auth resolvers.
 *
 */
public class AuthResolverFactory implements ParamResolverFactory<Auth> {

    private final AuthResolver authResolver;

    @Inject
    public AuthResolverFactory(AuthResolver authResolver) {
        this.authResolver = authResolver;
    }

    @Override
    public ParamResolver<Auth> createParamResolver(Parameter parameter) {
        return authResolver;
    }

}