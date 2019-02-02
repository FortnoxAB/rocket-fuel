package auth.openid;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fortnox.reactivewizard.jaxrs.WebException;

import javax.validation.constraints.NotNull;

import static auth.openid.OpenIdClaims.*;

public class OpenIdValidator {

	private static final Logger LOG = LoggerFactory.getLogger(OpenIdValidator.class);



	public ImmutableOpenIdToken validate(@NotNull String openIdToken) {
		final DecodedJWT decodedOpenId;
		try {
			decodedOpenId = JWT.decode(openIdToken);
		} catch (Throwable throwable) {
			LOG.warn("failed to parse openId jwt", throwable);
			throw new WebException(HttpResponseStatus.UNAUTHORIZED, throwable);
		}

		if(decodedOpenId.getClaim(NAME).isNull() || decodedOpenId.getClaim(EMAIL).isNull() || decodedOpenId.getClaim(PICTURE).isNull()) {
			LOG.warn("failed to validate openId jwt, missing properties in jwt");
			throw new WebException(HttpResponseStatus.UNAUTHORIZED);
		}


		return new ImmutableOpenIdToken(decodedOpenId.getClaim(NAME).asString(), decodedOpenId.getClaim(EMAIL).asString(), decodedOpenId.getClaim(PICTURE).asString());
	}
}
