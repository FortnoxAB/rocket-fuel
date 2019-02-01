package auth;

import api.auth.Auth;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import se.fortnox.reactivewizard.jaxrs.JaxRsRequest;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.util.Optional;

import static rx.Observable.error;
import static rx.Observable.just;

/**
 * When a resource method has a {@link Auth} in the signature, this parameter resolver will try
 * to bind the parameter to an auth instance, that can be used inside of the method for getting valuable information
 * about the signed in user.
 */
@Singleton
public class AuthResolverImpl implements AuthResolver {
	private static final Logger     LOG                             = LoggerFactory.getLogger(AuthResolverImpl.class);
	private static final String     FAILURE_TO_AUTHENTICATE_REQUEST = "failure to authenticate request";
	private static final String     APPLICATION_COOKIE              = "application";

	private final JwtAuthResolver jwtAuthResolver;

	@Inject
	public AuthResolverImpl(JwtAuthResolver jwtAuthResolver) {
		this.jwtAuthResolver = jwtAuthResolver;
	}

	/**
	 * Resolves a auth for a resource method as part of the authentication process. If the authentication
	 * fails, a unauthorized will be returned instead.
	 *
	 * @param request the request bound to a resource method with an Auth in the signature.
	 * @return Auth or an exception, if failure to authorize.
	 */
	@Override
	public Observable<Auth> resolve(JaxRsRequest request) {
		try {
			return resolveAuth(request);
		} catch (Throwable e) {
			LOG.warn("Unexpected error loading auth", e);
			return  error(new WebException(HttpResponseStatus.UNAUTHORIZED, FAILURE_TO_AUTHENTICATE_REQUEST));
		}
	}

	/**
	 * Resolves an auth through a JWT instance.
	 *
	 * @param request the current request
	 * @return Auth for the user
	 */
	private Observable<Auth> resolveAuth(JaxRsRequest request) {
		Optional<String> possibleApplicationCookie = Optional.ofNullable(request.getCookieValue(APPLICATION_COOKIE));

		if (!possibleApplicationCookie.isPresent()) {
			// for now, only allow login through cookie
			LOG.info("unauthorized request, failed to determine caller");
			return  error(new WebException(HttpResponseStatus.UNAUTHORIZED, FAILURE_TO_AUTHENTICATE_REQUEST));
		}

		return just(possibleApplicationCookie
			.map(jwtAuthResolver::getAuth)
			.orElseThrow(() -> new WebException(HttpResponseStatus.UNAUTHORIZED, FAILURE_TO_AUTHENTICATE_REQUEST)));
	}
}
