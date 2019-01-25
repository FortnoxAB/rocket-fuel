package impl;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import se.fortnox.reactivewizard.jaxrs.JaxRsRequest;
import se.fortnox.reactivewizard.jaxrs.WebException;

import static rx.Observable.just;

public class AuthResolverImplTest {

	private JwtParser jwtParser;

	private AuthResolver authResolver;

	@Before
	public void beforeEach() {

		jwtParser = Mockito.mock(JwtParser.class);
		authResolver = new AuthResolverImpl(jwtParser);
	}

	@Test
	public void shouldReturnUnauthorizedIfBothAuthorizationAndCookieValueIsMissingInRequest() {

		JaxRsRequest jaxRsRequest = Mockito.mock(JaxRsRequest.class);

		Mockito.when(jaxRsRequest.getHeader("Authorization")).thenReturn(null);
		Mockito.when(jaxRsRequest.getHeader("connect.sid")).thenReturn(null);

		try {
			authResolver.resolve(jaxRsRequest).toBlocking().single();
			Assert.fail("expected exception");
		} catch(WebException expectedException)  {
			Assert.assertEquals(HttpResponseStatus.UNAUTHORIZED, expectedException.getStatus());
		}
	}


	@Test
	public void shouldUseJwtTokenFoundInAuthorizationHeader() {

		JaxRsRequest jaxRsRequest = Mockito.mock(JaxRsRequest.class);

		Mockito.when(jaxRsRequest.getHeader("Authorization")).thenReturn("myJwtToken");
		Mockito.when(jaxRsRequest.getHeader("connect.sid")).thenReturn(null);

		Mockito.when(jwtParser.getAuth(Matchers.anyString())).thenReturn(just(new AuthImpl()));

		authResolver.resolve(jaxRsRequest).toBlocking().single();

		Mockito.verify(jwtParser, Mockito.times(1)).getAuth("myJwtToken");
	}



}