package auth.openid;

import auth.ClockProvider;
import auth.JwkResource;
import auth.JwkResponse;
import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.Jwk;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.WebException;

import javax.validation.constraints.NotNull;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static auth.openid.OpenIdClaims.*;
import static rx.Observable.just;
import static se.fortnox.reactivewizard.util.rx.RxUtils.exception;

/**
 * Validates openIds. A jwk endpoint must be provided in the @{@link OpenIdConfiguration}
 * so that the validation can be performed.
 *
 * Only SHA256withRSA ( specified as RS256 in jwt) is supported.
 *
 * Id ( specified as kid in jwt) is a mandatory field for the jwk tokens.
 *
 */
public class OpenIdValidator {

    private static final Logger LOG = LoggerFactory.getLogger(OpenIdValidator.class);

    private final JwkResource jwkResource;
    private final OpenIdConfiguration openIdConfiguration;
    private final ClockProvider clockProvider;


    @Inject
    public OpenIdValidator(OpenIdConfiguration openIdConfiguration, JwkResource jwkResource, ClockProvider clockProvider) {
        this.jwkResource = jwkResource;
        this.openIdConfiguration = openIdConfiguration;
        this.clockProvider = clockProvider;
    }

    /**
     * Will validate that the given openId jwt is valid and contains all needed fields.
     *
     * A @{@link WebException} with httpResponseStatus UNAUTHORIZED will be returned if the validation fails. The cause will be
     * included in the web exception. This is so that the user never will now the cause of the failure of the validation,
     * if it would, it could be a security issue.
     *
     * @param openIdToken openId jwt token
     * @return a openId token object.
     */
    public Observable<ImmutableOpenIdToken> validate(@NotNull String openIdToken) {
        return verify(openIdToken)
                .map(this::asImmutableOpenIdToken)
                .onErrorResumeNext(e -> exception(() -> new WebException(HttpResponseStatus.UNAUTHORIZED)));
    }

    private ImmutableOpenIdToken asImmutableOpenIdToken(DecodedJWT decodedOpenId) {
        return new ImmutableOpenIdToken(decodedOpenId.getClaim(NAME).asString(), decodedOpenId.getClaim(EMAIL).asString(), decodedOpenId.getClaim(PICTURE).asString());
    }

    /**
     * Verifies and decodes to OpenId token. Will validate the token with google as well as check the integrity
     * of the jwt
     *
     * @param token the unverified jwt openID token
     * @return a jwtVerifier
     */
    private Observable<DecodedJWT> verify(String token) {
        final String issuer = openIdConfiguration.getIssuer();
        final DecodedJWT unverifiedJwt = JWT.decode(token);
        final String keyId = unverifiedJwt.getKeyId();
        return jwkResource.getJWks()
                .doOnError((e) -> LOG.error("failed to get jwks to verify token", e))
                .map(OpenIdValidator::getJwksOrderedById)
                .doOnError((e) -> LOG.error("failed to order jwks", e))
                .flatMap(jwksById -> getPublicKeyByKeyId(keyId, jwksById))
                .doOnError((e) -> LOG.info("failed to get jwk by kid", e))
                .map(publicKey -> getJwtVerifier(issuer, publicKey))
                .doOnError((e) -> LOG.info("failed to create jwt verifier", e))
                .map(jwtVerifier -> jwtVerifier.verify(token))
                .doOnError(e -> LOG.info("failed to verify token", e));
    }

    private JWTVerifier getJwtVerifier(String issuer, RSAPublicKey publicKey) {
        final Algorithm rsa = Algorithm.RSA256(publicKey, null);
        JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWT.require(rsa)
                .withIssuer(issuer)
                .withAudience(openIdConfiguration.getClientId())
                .acceptLeeway(Long.MAX_VALUE); // Don't check for expiration.
        return verification.build(clockProvider.getClock());
    }

    /**
     * constructs a map where the key is the id ( kid ) of the jwt.
     */
    private static Map<String, Jwk> getJwksOrderedById(JwkResponse response) {
        return response.getKeys().stream()
                .map(jwk -> new Jwk(
                        jwk.getKid(),
                        jwk.getKty(),
                        jwk.getAlg(),
                        jwk.getUse(),
                        jwk.getKey_ops(),
                        jwk.getX5u(),
                        jwk.getX5c(),
                        jwk.getX5t(),
                        jwk.getAdditionalAttributes()))
                .collect(Collectors.toMap(Jwk::getId, Function.identity()));
    }

    private static Observable<RSAPublicKey> getPublicKeyByKeyId(String keyId, Map<String, Jwk> jwksById) {
        try {
            if (!jwksById.containsKey(keyId)) {
                return Observable.error(new WebException(HttpResponseStatus.UNAUTHORIZED, "jwk key not found"));
            }
            return just((RSAPublicKey) jwksById.get(keyId).getPublicKey());
        } catch (InvalidPublicKeyException e) {
            return Observable.error(new WebException(HttpResponseStatus.UNAUTHORIZED, e));
        }
    }
}
