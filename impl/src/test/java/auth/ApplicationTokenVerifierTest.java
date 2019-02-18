package auth;

import auth.application.ApplicationTokenConfig;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.Clock;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class ApplicationTokenVerifierTest {
	private static final String VALID_JWT = "eyJhbGciOiJIUzI1NiIsImtpZCI6ImIxNWEyYjhmN2E2YjNmNmJjMDhiYzFjNTZhODg0MTBl" +
		"MTQ2ZDAxZmQiLCJ0eXAiOiJKV1QifQ.eyJlbWFpbCI6ImplcHAzX2RyYWdvbnNsYXllckBmb3J0bm94LnNlIiwibmFtZSI6ImplcHAzI" +
		"iwicGljdHVyZSI6InVybHRvcGljdHVyZSIsInVzZXJfaWQiOjEsImlhdCI6MTU0ODQxNzY0NywiZXhwIjoxNTQ4NDIxMjQ3LCJpc3Mi" +
		"OiJyb2NrZXQtZnVlbCJ9.PTN4omIFwoeS_TyaTXZzwoW9cNgvZViCwMJC_Pt_kCQ";
	private ApplicationTokenVerifier applicationTokenVerifier;
	private ApplicationTokenConfig   applicationTokenConfig;
	private ClockProvider            clockProvider;

	@Before
	public void before() {
		long milliseconds = LocalDateTime.parse("2019-01-25T12:14:17.290").toInstant(ZoneOffset.UTC).toEpochMilli();
		Date currentTime  = new Date(milliseconds);
		applicationTokenConfig = new ApplicationTokenConfig();
		applicationTokenConfig.setSecret("my-super-secret-that-is-long-enough");
		clockProvider = () -> (Clock)() -> currentTime;

		applicationTokenVerifier = new ApplicationTokenVerifier(clockProvider, applicationTokenConfig);
	}

	@Test(expected = SignatureVerificationException.class)
	public void shouldThrowExceptionIfIssuerIsNotRocketFuel() {
		String withoutIssuer = "eyJhbGciOiJIUzI1NiIsImtpZCI6ImIxNWEyYjhmN2E2YjNmNmJjMDhiYzFjNTZhODg0MTBlMTQ2ZDAxZmQiLCJ0" +
			"eXAiOiJKV1QifQ.eyJlbWFpbCI6ImplcHAzX2RyYWdvbnNsYXllckBmb3J0bm94LnNlIiwibmFtZSI6ImplcHAzIiwicGljdHV" +
			"yZSI6InVybHRvcGljdHVyZSIsInVzZXJfaWQiOjEsImlhdCI6MTU0ODQxNzY0NywiZXhwIjoxNTQ4NDIxMjQ3fQ.mu6GzPQfLVL" +
			"9jVatdN-Q6CSNuYEA3LuCtwma-m-8jKU";

		applicationTokenVerifier.verifyAndDecode(withoutIssuer);
	}

	@Test(expected = SignatureVerificationException.class)
	public void shouldThrowExceptionIfSecretDoesNotMatch() {
		applicationTokenConfig.setSecret("wrong-secret");
		applicationTokenVerifier = new ApplicationTokenVerifier(clockProvider, applicationTokenConfig);

		applicationTokenVerifier.verifyAndDecode(VALID_JWT);

	}

	@Test
	public void shouldReturnDecodedJWtIfVerificationIsSuccessful() {
		DecodedJWT decodedJwt = applicationTokenVerifier.verifyAndDecode(VALID_JWT);

		Map<String, Claim> claims = decodedJwt.getClaims();

		assertNotNull(claims.get("user_id"));
		assertNotNull(claims.get("email"));
		assertNotNull(claims.get("picture"));
		assertNotNull(claims.get("name"));
		assertNotNull(claims.get("exp"));

	}

}