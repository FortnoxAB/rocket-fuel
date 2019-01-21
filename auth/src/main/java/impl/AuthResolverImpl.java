package impl;

import api.Auth;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.oracle.tools.packager.Log;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.JaxRsRequest;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.Optional;

import static rx.Observable.error;
import static rx.Observable.just;

/**
 * When a resource method has a {@link Auth} in the signature this parameter resolver will try
 * to bind a auth instance, that can be used inside of the method for getting valuable information
 * about the signed in user.
 *
 */
@Singleton
public class AuthResolverImpl implements AuthResolver {
    private static final Logger log = LoggerFactory.getLogger(AuthResolverImpl.class);

    @Inject
    public AuthResolverImpl() { }

    /**
     * Resolves a auth to resource method as part of the authorization process. If the authorization
     * fails, a unauthorized will be returned instead.
     *
     * @param request the request bound to a resource method with an Auth in the signature.
     * @return Auth or an exception if failure to authorize.
     */
    @Override
    public Observable<Auth> resolve(JaxRsRequest request) {
        try {
            return resolveAuth(request);
        } catch (Throwable e) {
            log.warn("Unexpected error loading auth", e);
            return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
        }
    }

    private Observable<Auth> resolveAuth(JaxRsRequest request) {
        Optional<String> possibleAuthorization = Optional.ofNullable(request.getHeader("Authorization"));
        if(!possibleAuthorization.isPresent()) {
            Log.info("unauthorized request");
            return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
        }
        // TODO: this must be changed to a jwt parser and we need to make sure the caller is a valid caller.
        // TODO: expiration must  be taken in consideration.
        long userId =  Long.parseLong(possibleAuthorization.get());
        Auth auth = new AuthImpl(userId);
        return just(auth);
    }

}
