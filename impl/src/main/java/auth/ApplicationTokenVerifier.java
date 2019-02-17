package auth;

import auth.application.ApplicationTokenConfig;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Clock;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Singleton;

/**
 * Decodes and verifies application JWTs.
 * It creates a new and reusable instance of the {@link JWTVerifier} with the configuration {@link ApplicationTokenConfig} provided during
 * startup.
 */
@Singleton
public class ApplicationTokenVerifier {

    private JWTVerifier jwtVerifier;
    private static final String ISSUER = "rocket-fuel";
    private static final long LEEWAY = 5;

    public ApplicationTokenVerifier() {
        // used by guice
    }

    public ApplicationTokenVerifier(ClockProvider clockProvider, ApplicationTokenConfig applicationTokenConfig) {
        Algorithm algorithm = Algorithm.HMAC256(applicationTokenConfig.getSecret());
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWT.require(algorithm)
                .acceptLeeway(LEEWAY)
                .withIssuer(ISSUER)
                .acceptExpiresAt(LEEWAY);
        Clock clock = clockProvider.getClock();
        this.jwtVerifier = verification.build(clock);
    }

    /**
     * Perform the verification against the given Token, using any previous configured options.
     *
     * @param token to verify.
     * @return a verified and decoded JWT
     **/
    public DecodedJWT verifyAndDecode(String token) {
        return jwtVerifier.verify(token);
    }
}
