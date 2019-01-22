package impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
@Singleton
public class JwtVerifier {
    private static final Logger log = LoggerFactory.getLogger(JwtVerifier.class);
    private static final int AUTHORIZATION_PREFIX_LENGTH = "Bearer ".length();
    private final JWTVerifier jwtVerifier;

    @Inject
    public JwtVerifier(JwtConfig jwtConfig) {
        String key = jwtConfig.getKey();
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Bad JWT key");
        }
        Algorithm algorithm = Algorithm.HMAC256(key);
        jwtVerifier = JWT.require(algorithm).build();
    }

    public Optional<DecodedJWT> verifyAndDecode(String authorizationHeader) {
        if (jwtVerifier != null && authorizationHeader != null && !authorizationHeader.isEmpty()) {
            try {
                DecodedJWT jwt = jwtVerifier.verify(authorizationHeader.substring(AUTHORIZATION_PREFIX_LENGTH));
                return Optional.of(jwt);
            } catch (JWTVerificationException e) {
                log.warn("Got bad JWT: "+authorizationHeader, e);
            }
        }
        return Optional.empty();
    }
}