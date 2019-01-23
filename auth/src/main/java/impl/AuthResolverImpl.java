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
import java.util.Objects;
import java.util.Optional;

import static rx.Observable.error;
import static rx.Observable.just;

/**
 * When a resource method has a {@link Auth} in the signature, this parameter resolver will try
 * to bind the parameter to an auth instance, that can be used inside of the method for getting valuable information
 * about the signed in user.
 */
@Singleton
public class AuthResolverImpl implements AuthResolver {
    private static final Logger LOG = LoggerFactory.getLogger(AuthResolverImpl.class);
    private static final String FAILURE_TO_AUTHENTICATE_REQUEST = "failure to authenticate request";
    private static final Observable<Auth> UNAUTHORIZED = error(new WebException(HttpResponseStatus.UNAUTHORIZED, FAILURE_TO_AUTHENTICATE_REQUEST));
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final JwtAuthenticator jwtAuthenticator;
    private final UserDao userResource;

    @Inject
    public AuthResolverImpl(JwtAuthenticator jwtAuthenticator, UserDao userResource) {
        this.userResource = userResource;
        Objects.requireNonNull(jwtAuthenticator);
        this.jwtAuthenticator = jwtAuthenticator;
    }

    /**
     * Resolves a auth for a resource method as part of the authentication process. If the authentication
     * fails, a unauthorized will be returned instead.
     *
     * @param request the request bound to a resource method with an Auth in the signature.
     * @return Auth or an exception, if failure to authorize.
     */
    @Override
    public Observable<Auth> resolve(JaxRsRequest request) {
        try {
            return resolveAuth(request);
        } catch (Throwable e) {
            LOG.warn("Unexpected error loading auth", e);
            return UNAUTHORIZED;
        }
    }

    /**
     * Resolves an auth through a JWT instance.
     * @param request the current request
     * @return Auth for the user
     */
    private Observable<Auth> resolveAuth(JaxRsRequest request) {
        Optional<String> possibleAuthorization = Optional.ofNullable(request.getHeader("Authorization"));
        Optional<String> possibleCookieValue = Optional.ofNullable(request.getCookieValue("connect.sid"));
        if (!possibleAuthorization.isPresent() && !possibleCookieValue.isPresent()) {
            LOG.info("unauthorized request");
            return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
        }
        Observable<String> resolveToken = getToken(possibleAuthorization, possibleCookieValue);
        return toBeReplacedByJWtVerifier(resolveToken);

    }

    private Observable<String> getToken(Optional<String> possibleAuthorization, Optional<String> possibleCookieValue) {
        // Authorize cookie, set authorization
        return possibleAuthorization
                .map(Observable::just).orElseGet(() -> resolveTokenFromAuthService(possibleCookieValue.get()));
    }

    private static Observable<Auth> toBeReplacedByJWtVerifier(Observable<String> resolveToken) {
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
                        auth = new AuthImpl("",null);
                        auth.setUserId(userId);
                    } catch (NumberFormatException ignored) {
                        Claim sub = JWT.decode(s).getClaim("sub");
                        String subValue = sub.asString();
                        // Resolve subValue to userId somehow
                        auth = new AuthImpl("",null);
                        auth.setUserId(-1);
                    }
                    return auth;
                });
    }

    private static Observable<String> resolveTokenFromAuthService(String possibleCookieValue) {
        return HttpClient.newClient(new InetSocketAddress("authproxy", 3000))
                .createGet("/verify")
                .addCookie(new DefaultCookie("connect.sid", possibleCookieValue))
                .flatMap(response -> {
                    if (response.getStatus().equals(HttpResponseStatus.UNAUTHORIZED)) {
                        return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
                    }
                    return just(response.getHeader("Authorization"));
                });
    }

}
