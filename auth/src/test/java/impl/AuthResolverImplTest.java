package impl;

import api.Auth;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import se.fortnox.reactivewizard.jaxrs.JaxRsRequest;
import se.fortnox.reactivewizard.jaxrs.WebException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static rx.Observable.just;

public class AuthResolverImplTest {

	private JwtParser jwtParser;

	private AuthResolver authResolver;

	private AuthDao authDao;

	@Before
	public void beforeEach() {
		authDao = mock(AuthDao.class);
		jwtParser = mock(JwtParser.class);
		authResolver = new AuthResolverImpl(jwtParser, authDao);
	}

	@Test
	public void shouldReturnUnauthorizedIfBothAuthorizationAndCookieValueIsMissingInRequest() {

		JaxRsRequest jaxRsRequest = mock(JaxRsRequest.class);
		when(jaxRsRequest.getHeader("Authorization")).thenReturn(null);
		when(jaxRsRequest.getHeader("connect.sid")).thenReturn(null);

		try {
			authResolver.resolve(jaxRsRequest).toBlocking().single();
			Assert.fail("expected exception");
		} catch(WebException expectedException)  {
			assertEquals(HttpResponseStatus.UNAUTHORIZED, expectedException.getStatus());
		}
	}


	@Test
	public void shouldUseJwtTokenFoundInAuthorizationHeaderAndFetchUserIdFromDao() {

		JaxRsRequest jaxRsRequest = mock(JaxRsRequest.class);
		when(authDao.getUserId(anyString())).thenReturn(just(3l));
		Mockito.when(jaxRsRequest.getHeader("Authorization")).thenReturn("myJwtToken");
		when(jaxRsRequest.getCookieValue("connect.sid")).thenReturn(null);

		when(jwtParser.getAuth(Matchers.anyString())).thenReturn(just(new AuthImpl()));

		Auth auth = authResolver.resolve(jaxRsRequest).toBlocking().single();

		Mockito.verify(jwtParser, times(1)).getAuth("myJwtToken");
		assertEquals(3, auth.getUserId());
        verify(authDao, times(1)).getUserId(anyString());

	}


	@Test
	public void shouldUseJwtTokenFoundInAuthorizationHeaderAndFetchUserIdFromCookie() {
		JaxRsRequest jaxRsRequest = mock(JaxRsRequest.class);
		Mockito.when(jaxRsRequest.getHeader("Authorization")).thenReturn("myJwtToken");
        Mockito.when(jaxRsRequest.getCookieValue("connect.sid")).thenReturn(null);
        Mockito.when(jaxRsRequest.getCookieValue("application.user")).thenReturn("3");


		when(jwtParser.getAuth(Matchers.anyString())).thenReturn(just(new AuthImpl()));
        when(jwtParser.getUserId(anyString())).thenReturn(just(3L));
		Auth auth = authResolver.resolve(jaxRsRequest).toBlocking().single();

		Mockito.verify(jwtParser, times(1)).getAuth("myJwtToken");
		assertEquals(3, auth.getUserId());
		verify(authDao, times(0)).getUserId(anyString());
	}

    @Test
    public void shouldUseJwtTokenFoundInAuthorization() {
        JaxRsRequest jaxRsRequest = mock(JaxRsRequest.class);
        Mockito.when(jaxRsRequest.getHeader("Authorization")).thenReturn("myJwtToken");
        Mockito.when(jaxRsRequest.getCookieValue("connect.sid")).thenReturn(null);
        Mockito.when(jaxRsRequest.getCookieValue("application.user")).thenReturn("3");


        when(jwtParser.getAuth(Matchers.anyString())).thenReturn(just(new AuthImpl()));
        when(jwtParser.getUserId(anyString())).thenReturn(just(3L));
        Auth auth = authResolver.resolve(jaxRsRequest).toBlocking().single();

        Mockito.verify(jwtParser, times(1)).getAuth("myJwtToken");
        assertEquals(3, auth.getUserId());
        verify(authDao, times(0)).getUserId(anyString());
    }



}