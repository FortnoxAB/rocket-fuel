package impl;

import api.Auth;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponseStatus;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static rx.Observable.error;
import static rx.Observable.just;

public class JwtAuthenticator {

    public static final String CLAIM_EMAIL = "email";
    private final JwtVerifier jwtVerifier;

    @Inject
    public JwtAuthenticator(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    /**
     *
     * Returns a auth instance from a jwt.
     *
     * @param authorizationHeader the jwt passed as header from the client request
     * @return auth for the given user.
     */
    public Observable<Auth> getAuth(String authorizationHeader) {
        final Optional<DecodedJWT> decodedJWT = jwtVerifier.verifyAndDecode(authorizationHeader);
        if (!decodedJWT.isPresent()) {
            return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
        }

        final DecodedJWT jwt = decodedJWT.get();
        Optional<String> email = getEmail(jwt.getClaims());
        Optional<OffsetDateTime> expires = getExpiration(jwt);
        if(!email.isPresent() || !expires.isPresent()) {
            return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
        }

        return just(new AuthImpl(email.get(),expires.get()));
    }

    private static Optional<String> getEmail(Map<String, Claim> claims) {
        Claim emailAddress = claims.get(CLAIM_EMAIL);
        if (!emailAddress.isNull()) {
            return Optional.empty();
        }
        return Optional.of(emailAddress.asString());
    }

    private static Optional<OffsetDateTime> getExpiration(DecodedJWT jwt) {
        Date expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) {
            return Optional.empty();
        }
        return Optional.of(OffsetDateTime.ofInstant(expiresAt.toInstant(), ZoneId.systemDefault()));
    }
}