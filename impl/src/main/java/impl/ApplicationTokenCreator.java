package impl;

import api.auth.ApplicationToken;
import auth.application.ApplicationTokenConfig;
import auth.openid.OpenIdValidator;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.Inject;
import dates.DateProvider;

import java.util.Date;

import static auth.application.ApplicationTokenClaims.USER_ID;
import static auth.openid.OpenIdClaims.*;

class ApplicationTokenCreator {

    private ApplicationTokenConfig applicationTokenConfig;

    private DateProvider dateProvider;

    @Inject
    public ApplicationTokenCreator(ApplicationTokenConfig applicationTokenConfig, DateProvider dateProvider) {
        this.applicationTokenConfig = applicationTokenConfig;
        this.dateProvider = dateProvider;
    }

    ApplicationToken createApplicationToken(OpenIdValidator.ImmutableOpenIdToken openIdToken, long userId) {

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
