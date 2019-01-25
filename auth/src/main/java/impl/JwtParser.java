package impl;

import api.Auth;
import com.auth0.jwt.JWT;
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

public class JwtParser {

    private static final String CLAIM_EMAIL          = "email";
    private static final String CLAIM_NAME           = "name";
    private static final String CLAIM_EMAIL_VERIFIED = "email_verified";
    private static final String CLAIM_PICTURE        = "picture";

    private final DateProvider dateProvider;

    @Inject
    public JwtParser(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    /**
     *
     * Returns a auth instance from a jwt.
     *
     * @param authorizationHeader the jwt passed as header from the client request
     * @return auth for the given user.
     */
    public Observable<Auth> getAuth(String authorizationHeader) {
        final Optional<DecodedJWT> decodedJWT = Optional.of(JWT.decode(authorizationHeader));
        if (!decodedJWT.isPresent()) {
            return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
        }

        final DecodedJWT jwt = decodedJWT.get();

        Claim email = jwt.getClaim(CLAIM_EMAIL);
        Claim name = jwt.getClaim(CLAIM_NAME);
        Claim emailVerified = jwt.getClaim(CLAIM_EMAIL_VERIFIED);
        Claim picture = jwt.getClaim(CLAIM_PICTURE);
        Optional<OffsetDateTime> expires = getExpiration(jwt);

        if(email.isNull() || name.isNull() || emailVerified.isNull() || !expires.isPresent()) {
            return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
        }

        if(expires.get().isBefore(dateProvider.getOffsetDateTime())){
            return error(new WebException(HttpResponseStatus.UNAUTHORIZED));
        }

        Auth auth = new AuthImpl();
        auth.setEmail(email.asString());
        auth.setName(name.asString());
        auth.setExpires(expires.get());
        auth.setPicture(picture.asString());
        return just(auth);
    }

    private static Optional<OffsetDateTime> getExpiration(DecodedJWT jwt) {
        Date expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) {
            return Optional.empty();
        }
        return Optional.of(OffsetDateTime.ofInstant(expiresAt.toInstant(), ZoneId.systemDefault()));
    }
}