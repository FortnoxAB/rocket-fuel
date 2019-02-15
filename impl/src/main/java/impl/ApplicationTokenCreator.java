package impl;

import api.auth.ApplicationToken;
import auth.application.ApplicationTokenConfig;
import auth.openid.ImmutableOpenIdToken;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.Inject;
import dates.DateProvider;

import java.util.Date;

import static auth.application.ApplicationTokenClaims.USER_ID;
import static auth.openid.OpenIdClaims.*;

/**
 * Handles creation of application tokens for the client.
 *
 * The application tokens are used by the clients to authenticate the user when accessing restricted endpoints.
 */
public class ApplicationTokenCreator {

    private ApplicationTokenConfig applicationTokenConfig;

    private DateProvider dateProvider;

    @Inject
    public ApplicationTokenCreator(ApplicationTokenConfig applicationTokenConfig, DateProvider dateProvider) {
        this.applicationTokenConfig = applicationTokenConfig;
        this.dateProvider = dateProvider;
    }

    /**
     * Creates an applicationToken based on the openId token and a user id.
     *
     *
     * @param openIdToken a openId token
     * @param userId the userId that shall be connected to the information in the openId token.
     * @return an applicationToken as a jwt containing combined information from both the application as well
     * as the authorization process.
     */
    ApplicationToken createApplicationToken(ImmutableOpenIdToken openIdToken, long userId) {

        long epochMilli = dateProvider.getOffsetDateTime().plusHours(1).toInstant().toEpochMilli();
        Date date = new Date(epochMilli);

        final String jwt = JWT.create()
            .withClaim(NAME,openIdToken.name)
            .withClaim(EMAIL, openIdToken.email)
            .withClaim(USER_ID, userId)
            .withClaim(PICTURE, openIdToken.picture)
            .withExpiresAt(date)
            .sign(Algorithm.HMAC256(applicationTokenConfig.getSecret()));

        return new ApplicationToken(jwt);
    }
}
