package impl;

import api.Auth;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.reactivex.netty.protocol.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.JaxRsRequest;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.net.InetSocketAddress;
import java.util.Optional;

import static rx.Observable.error;
import static rx.Observable.just;

/**
 * When a resource method has a {@link Auth} in the signature this parameter resolver will try
 * to bind a auth instance, that can be used inside of the method for getting valuable information
 * about the signed in user.
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
        Optional<String> possibleCookieValue = Optional.ofNullable(request.getCookieValue("connect.sid"));
        if (!possibleAuthorization.isPresent() && !possibleCookieValue.isPresent()) {
            log.info("unauthorized request");
            return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
        }
        Observable<String> resolveToken;
        if (possibleAuthorization.isPresent()) {
            resolveToken = just(possibleAuthorization.get());
        } else {
            // Authorize cookie, set authorization
            resolveToken = HttpClient.newClient(new InetSocketAddress("localhost", 3000))
                    .unsafeSecure()
                    .createGet("/verify")
                    .addCookie(new DefaultCookie("connect.sid", possibleCookieValue.get()))
                    .flatMap(response -> {
                        if (response.getStatus().equals(HttpResponseStatus.UNAUTHORIZED)) {
                            return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
                        }
                        return just(response.getHeader("Authorization"));
                    });
        }
        return resolveToken
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof WebException) {
                        return just("123");
                    }
                    return error(throwable);
                })
                .map(s -> {
                    // TODO: this must be changed to a jwt parser and we need to make sure the caller is a valid caller.
                    // TODO: expiration must  be taken in consideration.
                    Auth auth;
                    try {
                        long userId = Long.parseLong(s);
                        auth = new AuthImpl(userId);
                    } catch (NumberFormatException ignored) {
                        Claim sub = JWT.decode(s).getClaim("sub");
                        String subValue = sub.asString();
                        // Resolve subValue to userId somehow
                        auth = new AuthImpl(-1L);
                    }

                    return auth;
                });
    }

}
