package impl;

import api.ApplicationToken;
import auth.ApplicationTokenConfig;
import auth.OpenIdValidatorImpl;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.Inject;

import static auth.OpenIdClaims.*;

class ApplicationTokenCreator {
    //TODO: remove the auth module. We will need to have it in one place, because the validation needs to be aware of userId

    private ApplicationTokenConfig applicationTokenConfig;

    private static final String USER_ID = "user_id";


    @Inject
    public ApplicationTokenCreator(ApplicationTokenConfig applicationTokenConfig) {
        this.applicationTokenConfig = applicationTokenConfig;
    }

    ApplicationToken createApplicationToken(OpenIdValidatorImpl.ImmutableOpenIdToken openIdToken, long userId) {
        final String jwt = JWT.create()
            .withClaim(NAME,openIdToken.name)
            .withClaim(EMAIL, openIdToken.email)
            .withClaim(USER_ID, userId)
            .withClaim(PICTURE, openIdToken.picture).sign(Algorithm.HMAC256(applicationTokenConfig.getSecret()));

        return new ApplicationToken(jwt);
    }
}
