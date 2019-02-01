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
import java.util.Optional;

import static rx.Observable.error;
import static rx.Observable.just;

public class JwtAuthResolver {

	private static final String CLAIM_EMAIL          = "email";
	private static final String CLAIM_NAME           = "name";
	private static final String CLAIM_PICTURE        = "picture";
	private static final String CLAIM_USER_ID        = "user_id";
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
		final DecodedJWT applicationTokenJwt = JWT.decode(rawApplicationJwt);

		Claim                    email         = applicationTokenJwt.getClaim(CLAIM_EMAIL);
		Claim                    name          = applicationTokenJwt.getClaim(CLAIM_NAME);
		Claim                    picture       = applicationTokenJwt.getClaim(CLAIM_PICTURE);
		Claim                    userId        = applicationTokenJwt.getClaim(CLAIM_USER_ID);

		Optional<OffsetDateTime> expires       = getExpiration(applicationTokenJwt);

		if (email.isNull() || name.isNull()  || !expires.isPresent() || userId.isNull()) {
			throw new WebException(HttpResponseStatus.UNAUTHORIZED);
		}

		if (expires.get().isBefore(dateProvider.getOffsetDateTime())) {
			throw new WebException(HttpResponseStatus.UNAUTHORIZED);
		}

		Auth auth = new AuthImpl();
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
	private static Optional<OffsetDateTime> getExpiration(DecodedJWT jwt) {
		Date expiresAt = jwt.getExpiresAt();
		if (expiresAt == null) {
			return Optional.empty();
		}
		return Optional.of(OffsetDateTime.ofInstant(expiresAt.toInstant(), ZoneId.systemDefault()));
	}
}