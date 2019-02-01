package impl;

import api.Auth;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Before;
import org.junit.Test;
import se.fortnox.reactivewizard.jaxrs.WebException;

import java.time.OffsetDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtAuthenticatorTest {
	private static final String JWT = "eyJhbGciOiJIUzI1NiIsImtpZCI6ImIxNWEyYjhmN2E2YjNmNmJjMDhiYzFjNTZhODg0MTBlMTQ2ZDAxZmQiLCJ0eXAiOiJKV1QifQ.eyJlbWFpbCI6ImplcHAzX2RyYWdvbnNsYXllckBmb3J0bm94LnNlIiwibmFtZSI6ImplcHAzIiwicGljdHVyZSI6InVybHRvcGljdHVyZSIsInVzZXJfaWQiOjEsImlhdCI6MTU0ODQxNzY0NywiZXhwIjoxNTQ4NDIxMjQ3fQ.yDPCbMe5ZPqNubrWBoJJytk3DqS5FiEVotirtj3wzNA";
	private JwtAuthResolver jwtAuthResolver;
	private DateProvider    dateProvider;
	@Before
	public void beforeEach() {
		dateProvider = mock(DateProvider.class);
		final String pastTime = "2019-01-01T14:00:47+01:00";
		when(dateProvider.getOffsetDateTime()).thenReturn(OffsetDateTime.parse(pastTime));
		jwtAuthResolver = new JwtAuthResolver(dateProvider);
	}

	@Test
	public void shouldResolveEmailFromJwt() {
		Auth auth = jwtAuthResolver.getAuth(JWT);
		assertEquals("jepp3_dragonslayer@fortnox.se", auth.getEmail());
	}

	@Test
	public void shouldResolveNameFromJwt() {
		Auth auth = jwtAuthResolver.getAuth(JWT);
		assertEquals("jepp3", auth.getName());
	}

	@Test
	public void shouldResolveExpires() {
		Auth auth = jwtAuthResolver.getAuth(JWT);
		assertEquals(OffsetDateTime.parse("2019-01-25T14:00:47+01:00"), auth.getExpires());
	}

	@Test
	public void shouldResolvePicture() {
		Auth auth = jwtAuthResolver.getAuth(JWT);
		assertEquals("urltopicture", auth.getPicture());
	}

	@Test
	public void shouldResolveUserId() {
		Auth auth = jwtAuthResolver.getAuth(JWT);
		assertEquals(1, auth.getUserId());
	}

	@Test
	public void shouldThrowIfTokenIsExpired() {
		when(dateProvider.getOffsetDateTime()).thenReturn(OffsetDateTime.parse("2030-01-01T14:00:47+01:00"));
		try {
			jwtAuthResolver.getAuth(JWT);
			fail("expected expection");
		} catch (WebException expectedException) {
			assertEquals(HttpResponseStatus.UNAUTHORIZED, expectedException.getStatus());
		}
	}

}