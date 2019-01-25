package impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Before;
import org.junit.Test;
import se.fortnox.reactivewizard.jaxrs.JaxRsRequest;
import se.fortnox.reactivewizard.jaxrs.WebException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.just;

public class AuthResolverImplTest {

	private JwtParser jwtParser;

	private AuthResolver authResolver;

	@Before
	public void beforeEach() {

		jwtParser = mock(JwtParser.class);
		authResolver = new AuthResolverImpl(jwtParser);
	}

	@Test
	public void shouldReturnUnauthorizedIfBothAuthorizationAndCookieValueIsMissingInRequest() {

		JaxRsRequest jaxRsRequest = mock(JaxRsRequest.class);

		when(jaxRsRequest.getHeader("Authorization")).thenReturn(null);
		when(jaxRsRequest.getHeader("connect.sid")).thenReturn(null);

		try {
			authResolver.resolve(jaxRsRequest).toBlocking().single();
			fail("expected exception");
		} catch(WebException expectedException)  {
			assertEquals(HttpResponseStatus.UNAUTHORIZED, expectedException.getStatus());
		}
	}


	@Test
	public void shouldUseJwtTokenFoundInAuthorizationHeader() {

		JaxRsRequest jaxRsRequest = mock(JaxRsRequest.class);

		when(jaxRsRequest.getHeader("Authorization")).thenReturn("myJwtToken");
		when(jaxRsRequest.getHeader("connect.sid")).thenReturn(null);

		when(jwtParser.getAuth(anyString())).thenReturn(just(new AuthImpl()));

		authResolver.resolve(jaxRsRequest).toBlocking().single();

		verify(jwtParser, times(1)).getAuth("myJwtToken");
	}



}