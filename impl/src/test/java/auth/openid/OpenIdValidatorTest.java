package auth.openid;

import auth.ClockProvider;
import auth.Jwk;
import auth.JwkResource;
import auth.JwkResponse;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.interfaces.Clock;
import com.google.common.collect.ImmutableList;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.log4j.Appender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.fortnox.reactivewizard.jaxrs.WebException;
import se.fortnox.reactivewizard.test.LoggingMockUtil;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.just;
import static se.fortnox.reactivewizard.test.TestUtil.matches;

public class OpenIdValidatorTest {

    private static final String OPEN_ID = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjdkNjgwZDhjNzBkNDRlOTQ3MTMzY2JkNDk5ZWJj" +
        "MWE2MWMzZDVhYmMiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI5MjE" +
        "zMTAzODczOTQtY2k0Mzd0ZnJjYzRyMW8zMGhxczNtcm5tcnBwNDBvajAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhd" +
        "WQiOiI5MjEzMTAzODczOTQtY2k0Mzd0ZnJjYzRyMW8zMGhxczNtcm5tcnBwNDBvajAuYXBwcy5nb29nbGV1c2VyY29udGVudC5" +
        "jb20iLCJzdWIiOiIxMDU0MzE0NDE1NTA3MjI0OTY2NDMiLCJlbWFpbCI6Implc3Blci5sYWhkZXZpcnRhQGdtYWlsLmNvbSIsI" +
        "mVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdF9oYXNoIjoidVdfZkYzcDN1SkxMMmJhYjFNUVllZyIsIm5hbWUiOiJKZXNwZXIgRXJ" +
        "sYW5kc3NvbiIsInBpY3R1cmUiOiJodHRwczovL2xoNS5nb29nbGV1c2VyY29udGVudC5jb20vLTNXb2U4RWdzcWtzL0FBQUFBQ" +
        "UFBQUFJL0FBQUFBQUFBQUtBLzVlY1oxVS1tZEpBL3M5Ni1jL3Bob3RvLmpwZyIsImdpdmVuX25hbWUiOiJKZXNwZXIiLCJmYW1" +
        "pbHlfbmFtZSI6IkVybGFuZHNzb24iLCJsb2NhbGUiOiJzdiIsImlhdCI6MTU1MDYxMDAwOSwiZXhwIjoxNTUwNjEzNjA5fQ.rf" +
        "5GuX5kFdz3DNgXW7zvH_XF5YOwzEk-H7PHrvaSXYD6knU6ARmAmGnRCK0sWmOvGEKt9R-99NY0O7gS1ynsKpQr2EIwdilCR8rW" +
        "2V3fcfvWZGQPc6vTp8ntNl3oWqe4sPBYm3pBzsWEr9Jm81LHsWieUzohvwA1KtVX1sxt21iFtKcS9-zSoX9JebRqs-DpnqlPaF" +
        "v42mN9wsap7oY1D1Xy_4-KWxujBuaLHhXjxv4My_MLR5crw1qeLotE1S4hI8edgYwntUz9Txd3wBfT-oVKFBbagMA81F5XMd9E" +
        "vDhiKjgg-Naz05ptZoOcvQJ4rWAqaXfmuejq4u1AKqHiMQ";

    private JwkResource         jwkResource = mock(JwkResource.class);
    private Appender            appender;
    private OpenIdConfiguration openIdConfiguration;
    private ClockProvider       clockProvider;

    @Before
    public void beforeEach() throws NoSuchFieldException, IllegalAccessException {
        appender = LoggingMockUtil.createMockedLogAppender(OpenIdValidator.class);
        openIdConfiguration = getValidOpenIdConfiguration();
        Date currentTime = getValidTimeInterval();
        clockProvider = () -> (Clock)() -> currentTime;

        JwkResponse jwkResponse = new JwkResponse();
        jwkResponse.setKeys(ImmutableList.of(getValidJwk()));
        when(jwkResource.getJWks()).thenReturn(just(jwkResponse));
    }

    @After
    public void afterEach() throws NoSuchFieldException, IllegalAccessException {
        LoggingMockUtil.destroyMockedAppender(appender, OpenIdValidator.class);
    }

    @Test
    public void shouldValidateSimpleJWT() {
        // given a valid config and a valid key
        OpenIdValidator openIdValidator = new OpenIdValidator(openIdConfiguration, jwkResource, clockProvider);

        // when
        ImmutableOpenIdToken token = openIdValidator.validate(OPEN_ID).toBlocking().single();

        // then the token should exist and contain proper values
        assertThat(token).isNotNull();
        assertThat(token.name).startsWith("Jesper");
        assertThat(token.email).startsWith("jesper");
        assertThat(token.picture).isNotNull();
    }

    @Test
    public void shouldNotValidateExpiredJWT() {

        // given a time when the jwt has expired
        long          milliseconds  = LocalDateTime.parse("2019-02-19T23:14:17.290").toInstant(ZoneOffset.ofHours(1)).toEpochMilli();
        Date          currentTime   = new Date(milliseconds);
        ClockProvider clockProvider = () -> (Clock)() -> currentTime;

        // when
        OpenIdValidator openIdValidator = new OpenIdValidator(openIdConfiguration, jwkResource, clockProvider);
        validateWithExpectedUnauthorized(openIdValidator);

        // then we should log that we could not verify the token
        verify(appender).doAppend(matches(log -> {
            assertThat(log.getLevel().toString()).isEqualTo("INFO");
            assertThat(log.getMessage().toString()).contains("failed to verify token");
        }));
    }

    @Test
    public void shouldAllowOneMinutesLeeway() {
        // given a time that is almost outside of the leeway timespan
        long milliseconds = LocalDateTime.parse("2019-02-19T21:59:09.290").toInstant(ZoneOffset.ofHours(1)).toEpochMilli();
        Date toEarly      = new Date(milliseconds);
        ClockProvider clockProvider = () -> (Clock)() -> toEarly;

        OpenIdValidator openIdValidator = new OpenIdValidator(openIdConfiguration, jwkResource, clockProvider);

        // when
        openIdValidator.validate(OPEN_ID).toBlocking().single();

        // then validation shall yield no errors
        assertThat(openIdValidator).isNotNull();
    }

    @Test
    public void shouldNotAllowMoreThanOneMinutesLeeway() {
        // given a time that just passed outisde of the leeway timespan
        long milliseconds = LocalDateTime.parse("2019-02-19T21:59:08.290").toInstant(ZoneOffset.ofHours(1)).toEpochMilli();
        Date toEarly      = new Date(milliseconds);

        ClockProvider clockProvider = () -> (Clock)() -> toEarly;

        // when
        OpenIdValidator openIdValidator = new OpenIdValidator(openIdConfiguration, jwkResource, clockProvider);

        try {
            openIdValidator.validate(OPEN_ID).toBlocking().single();
            fail("excepted exception");
        } catch (WebException e) {
            // then
            assertThat(e.getCause()).isNotNull();
            assertThat(e.getCause()).isInstanceOf(InvalidClaimException.class);
        }

    }

    private void validateWithExpectedUnauthorized(OpenIdValidator openIdValidator) {
        try {
            openIdValidator.validate(OPEN_ID).toBlocking().single();
            fail("expected exception");
        } catch (WebException e) {
            assertEquals("unauthorized", e.getError());
            assertEquals(HttpResponseStatus.UNAUTHORIZED, e.getStatus());
        }
    }

    @Test
    public void shouldNotValidateJwkWhenKeyCannotBeVerified() {
        // given no keys
        JwkResponse jwkResponse = new JwkResponse();
        jwkResponse.setKeys(Collections.emptyList());
        when(jwkResource.getJWks()).thenReturn(just(jwkResponse));

        // when
        OpenIdValidator openIdValidator = new OpenIdValidator(openIdConfiguration, jwkResource, clockProvider);
        validateWithExpectedUnauthorized(openIdValidator);

        // then we should log that we failed to get jwk by kid
        verify(appender).doAppend(matches(log -> {
            assertThat(log.getLevel().toString()).isEqualTo("INFO");
            assertThat(log.getMessage().toString()).contains("failed to get jwk by kid");
        }));
    }

    @Test
    public void shouldNotValidateJwkWhenIssuerIsIncorrect() {
        // given wrong issuer
        openIdConfiguration.setIssuer("wrong issuer");

        // when
        OpenIdValidator openIdValidator = new OpenIdValidator(openIdConfiguration, jwkResource, clockProvider);
        validateWithExpectedUnauthorized(openIdValidator);

        // then we should log that we could not verify the token
        verify(appender).doAppend(matches(log -> {
            assertThat(log.getLevel().toString()).isEqualTo("INFO");
            assertThat(log.getMessage().toString()).contains("failed to verify token");
        }));

    }

    @Test
    public void shouldNotValidateJwkWhenVerifierCannotBeCreated() {
        // given bad openid configuration
        ClockProvider clockProvider = () -> {
            throw new NullPointerException("poff");
        };

        // when
        OpenIdValidator openIdValidator = new OpenIdValidator(openIdConfiguration, jwkResource, clockProvider);
        validateWithExpectedUnauthorized(openIdValidator);

        // then we should log that we could not verify the token
        verify(appender).doAppend(matches(log -> {
            assertThat(log.getLevel().toString()).isEqualTo("INFO");
            assertThat(log.getMessage().toString()).contains("failed to verify token");
        }));
    }

    private OpenIdConfiguration getValidOpenIdConfiguration() {
        OpenIdConfiguration openIdConfiguration = new OpenIdConfiguration();
        openIdConfiguration.setClientId("921310387394-ci437tfrcc4r1o30hqs3mrnmrpp40oj0.apps.googleusercontent.com");
        openIdConfiguration.setJwksUri("https://www.googleapis.com/oauth2/v3/certs");
        openIdConfiguration.setIssuer("https://accounts.google.com");
        return openIdConfiguration;
    }

    private Jwk getValidJwk() {
        Jwk jwk = new Jwk();
        jwk.setId("7d680d8c70d44e947133cbd499ebc1a61c3d5abc");
        jwk.setType("RSA");
        jwk.setAlgorithm("RS256");
        jwk.setUsage("sig");
        jwk.setAdditionalAttributes("n", "2K7epoJWl_B68lRUi1txaa0kEuIK4WHiHpi1yC4kPyu48d046yLlrwuvbQMbog2YTOZdV" +
            "oG1D4zlWKHuVY00O80U1ocFmBl3fKVrUMakvHru0C0mAcEUQo7ItyEX7rpOVYtxlrVk6G8PY4" +
            "EK61EB-Xe35P0zb2AMZn7Tvm9-tLcccqYlrYBO4SWOwd5uBSqc_WcNJXgnQ-9sYEZ0JUMhKZel" +
            "EMrpX72hslmduiz-LMsXCnbS7jDGcUuSjHXVLM9tb1SQynx5Xz9xyGeN4rQLnFIKvgwpiqnvLpbMo6" +
            "grhJwrz67d1X6MwpKtAcqZ2V2v4rQsjbblNH7GzF8ZsfOaqw");
        jwk.setAdditionalAttributes("e", "AQAB");
        return jwk;
    }

    private Date getValidTimeInterval() {
        long milliseconds = LocalDateTime.parse("2019-02-19T22:14:17.290").toInstant(ZoneOffset.ofHours(1)).toEpochMilli();
        return new Date(milliseconds);
    }

}
