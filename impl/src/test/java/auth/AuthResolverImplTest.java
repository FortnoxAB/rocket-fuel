package auth;

import api.auth.Auth;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.fortnox.reactivewizard.jaxrs.JaxRsRequest;
import se.fortnox.reactivewizard.jaxrs.WebException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class AuthResolverImplTest {

	private JwtAuthResolver jwtAuthResolver;

	private AuthResolver authResolver;

	@Before
	public void beforeEach() {
		jwtAuthResolver = mock(JwtAuthResolver.class);
		authResolver = new AuthResolverImpl(jwtAuthResolver);
	}

	@Test
	public void shouldReturnUnauthorizedIfApplicationTokenCookieIsMissing() {

		JaxRsRequest jaxRsRequest = mock(JaxRsRequest.class);

		when(jaxRsRequest.getCookieValue("application")).thenReturn(null);

		try {
			authResolver.resolve(jaxRsRequest).toBlocking().single();
			Assert.fail("expected exception");
		} catch (WebException expectedException) {
			assertEquals(HttpResponseStatus.UNAUTHORIZED, expectedException.getStatus());
		}
	}

	@Test
	public void shouldResolveAuthIfApplicationCookieIsPresent() {

		JaxRsRequest jaxRsRequest = mock(JaxRsRequest.class);
		when(jaxRsRequest.getCookieValue("application")).thenReturn("myJwtToken");
		Auth authToResolve = new Auth();
		authToResolve.setUserId(3);
		when(jwtAuthResolver.getAuth(anyString())).thenReturn(authToResolve);
		Auth auth = authResolver.resolve(jaxRsRequest).toBlocking().single();
		Mockito.verify(jwtAuthResolver, times(1)).getAuth("myJwtToken");
		assertEquals(3, auth.getUserId());

	}

}