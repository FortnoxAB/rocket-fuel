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
	private static final String JWT = "eyJhbGciOiJIUzI1NiIsImtpZCI6ImIxNWEyYjhmN2E2YjNmNmJjMDhiYzFjNTZhODg0MTBlMTQ2ZDAxZmQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiJhc2Rhc2QiLCJhdWQiOiJhc2Rhc2QiLCJzdWIiOiIxMDIxOTkzMzkzMjk0NzUxNTkwNjUiLCJoZCI6ImZvcnRub3guc2UiLCJlbWFpbCI6ImplcHAzX2RyYWdvbnNsYXllckBmb3J0bm94LnNlIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJhc2RkIiwibmFtZSI6ImplcHAzIiwicGljdHVyZSI6InVybHRvcGljdHVyZSIsImdpdmVuX25hbWUiOiJKZXNwZXIiLCJmYW1pbHlfbmFtZSI6IkzDpGhkZXZpcnRhIiwibG9jYWxlIjoic3YiLCJpYXQiOjE1NDg0MTc2NDcsImV4cCI6MTU0ODQyMTI0N30._Taq1J9t75Rke_dedQseu_-SjNeN-XuRzCRHhsfBYps";
	private JwtParser    jwtParser;
	private DateProvider dateProvider;

	@Before
	public void beforeEach() {
		dateProvider = mock(DateProvider.class);
		final String pastTime = "2019-01-01T14:00:47+01:00";
		when(dateProvider.getOffsetDateTime()).thenReturn(OffsetDateTime.parse(pastTime));
		jwtParser = new JwtParser(dateProvider);
	}

	@Test
	public void shouldResolveEmailFromJwt() {
		Auth auth = jwtParser.getAuth(JWT).toBlocking().single();
		assertEquals("jepp3_dragonslayer@fortnox.se", auth.getEmail());
	}

	@Test
	public void shouldResolveNameFromJwt() {
		Auth auth = jwtParser.getAuth(JWT).toBlocking().single();
		assertEquals("jepp3", auth.getName());
	}

	@Test
	public void shouldResolveExpires() {
		Auth auth = jwtParser.getAuth(JWT).toBlocking().single();
		assertEquals(OffsetDateTime.parse("2019-01-25T14:00:47+01:00"), auth.getExpires());
	}

	@Test
	public void shouldResolvePicture() {
		Auth auth = jwtParser.getAuth(JWT).toBlocking().single();
		assertEquals("urltopicture", auth.getPicture());
	}

	@Test
	public void shouldThrowIfTokenIsExpired() {
		when(dateProvider.getOffsetDateTime()).thenReturn(OffsetDateTime.parse("2030-01-01T14:00:47+01:00"));
		try {
			jwtParser.getAuth(JWT).toBlocking().single();
			fail("expected expection");
		} catch (WebException expectedException) {
			assertEquals(HttpResponseStatus.UNAUTHORIZED, expectedException.getStatus());
		}
	}

}