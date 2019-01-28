package impl;

import api.Auth;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.reactivex.netty.protocol.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;
import se.fortnox.reactivewizard.jaxrs.JaxRsRequest;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.net.InetSocketAddress;
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
    private static final String COOKIE_CONNECT_SID = "connect.sid";
    private static final String COOKIE_USER = "application.user";

    private final JwtParser jwtParser;
    private AuthDao authDao;

    @Inject
    public AuthResolverImpl(JwtParser jwtParser, AuthDao authDao) {
        this.jwtParser = jwtParser;
        this.authDao = authDao;
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
     *
     * @param request the current request
     * @return Auth for the user
     */
    private Observable<Auth> resolveAuth(JaxRsRequest request) {
        Optional<String> possibleAuthorization = Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER));
        Optional<String> possibleCookieValue = Optional.ofNullable(request.getCookieValue(COOKIE_CONNECT_SID));
        Optional<String> possibleUserCookie = Optional.ofNullable(request.getCookieValue(COOKIE_USER));

        if (!possibleAuthorization.isPresent() && !possibleCookieValue.isPresent()) {
            LOG.info("unauthorized request");
            return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
        }
        final Observable<String> token;
        if (possibleAuthorization.isPresent()) {
            token = just(possibleAuthorization.get());
        } else {
            token = resolveTokenFromAuthService(possibleCookieValue.get());
        }
        // TODO: we need to set the cookie in the response if no exists ( we fetched from db )
        return token
                .flatMap(jwtParser::getAuth)
                .flatMap(appendUserId(possibleUserCookie))
                .doOnError((e) -> LOG.error("failed to construct auth from jwt", e));
    }

    private Func1<Auth, Observable<? extends Auth>> appendUserId(Optional<String> possibleUserCookie) {
        return (auth) -> {
            final Observable<Long> userId;
            if (possibleUserCookie.isPresent()) {
                userId = getUserIdFromCookie(possibleUserCookie.get());
            } else {
                userId = getUserIdFromDatabase(auth.getEmail());
            }
           return userId.map(id -> {
                auth.setUserId(id);
                return auth;
            });
        };
    }

    /**
     * Will load the user from the cookie, this is used for all requests that have been looked up in the database.
     * This is the prefered way. Getting the information from the database is more resource consuming.
     * @param possibleUserCookie
     * @return
     */
    private Observable<Long> getUserIdFromCookie(String possibleUserCookie) {
        return jwtParser.getUserId(possibleUserCookie);
    }

    /**
     * Will load the user from the database. The method will only be invoked the first time, when the user has no cookie.
     * If the user does not exist, we will create the user.
     *
     * @param email for the user we want to get from the database
     * @return userId mapping for the email.
     */
    private Observable<Long> getUserIdFromDatabase(String email) {
        LOG.info("fetching userId from the database");
        return authDao.getUserId(email);
    }


    /**
     * Resolves token from auth service. We should create a resource that we inject instead, add config for the uri as well.
     *
     * @param cookie a cookie that will sent to auth service to resolve a token.
     * @return a token that maps against the cookie.
     */
    @Deprecated
    private static Observable<String> resolveTokenFromAuthService(String cookie) {
        return HttpClient.newClient(new InetSocketAddress("authproxy", 3000))
                .createGet("/verify")
                .addCookie(new DefaultCookie(COOKIE_CONNECT_SID, cookie))
                .flatMap(response -> {
                    if (response.getStatus().equals(HttpResponseStatus.UNAUTHORIZED)) {
                        return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
                    }
                    return just(response.getHeader(AUTHORIZATION_HEADER));
                });
    }

}
