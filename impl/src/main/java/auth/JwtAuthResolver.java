package auth;

import api.auth.Auth;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import dates.DateProvider;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;

import static auth.application.ApplicationTokenClaims.USER_ID;
import static auth.openid.OpenIdClaims.*;

public class JwtAuthResolver {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthResolver.class);

    private final DateProvider dateProvider;

    @Inject
    public JwtAuthResolver(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    /**
     * Returns a auth instance from a jwt.
     *
     * @return auth for the given user.
     */
    public Auth getAuth(String rawApplicationJwt) {
        final DecodedJWT applicationTokenJwt;

        try {
            applicationTokenJwt = JWT.decode(rawApplicationJwt);
        } catch (Exception throwable) {
            LOG.warn("failed to decode application jwt");
            throw new WebException(HttpResponseStatus.UNAUTHORIZED);
        }

        Claim email = applicationTokenJwt.getClaim(EMAIL);
        Claim name = applicationTokenJwt.getClaim(NAME);
        Claim picture = applicationTokenJwt.getClaim(PICTURE);
        Claim userId = applicationTokenJwt.getClaim(USER_ID);

        Optional<OffsetDateTime> expires = getExpiration(applicationTokenJwt);

        if (email.isNull() || name.isNull() || !expires.isPresent() || userId.isNull()) {
            throw new WebException(HttpResponseStatus.UNAUTHORIZED);
        }

        // we must verify the token in a better way.
        if (expires.get().isBefore(dateProvider.getOffsetDateTime())) {
            throw new WebException(HttpResponseStatus.UNAUTHORIZED);
        }

        Auth auth = new Auth();
        auth.setEmail(email.asString());
        auth.setName(name.asString());
        auth.setExpires(expires.get());
        auth.setPicture(picture.asString());
        auth.setUserId(userId.asLong());
        return auth;
    }

    /**
     * Returns the expiration as an offset local date.
     *
     * @param jwt the decoded jwt we want the expiration date from
     * @return expiration date as OffsetDateTime instance
     */
    private  Optional<OffsetDateTime> getExpiration(DecodedJWT jwt) {
        Date expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) {
            return Optional.empty();
        }
        return Optional.of(OffsetDateTime.ofInstant(expiresAt.toInstant(), dateProvider.getDefaultZone()));
    }
}