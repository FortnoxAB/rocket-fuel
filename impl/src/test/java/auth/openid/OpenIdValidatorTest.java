package auth.openid;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Test;
import se.fortnox.reactivewizard.jaxrs.WebException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OpenIdValidatorTest {

    private static final String SIMPLE_JWT  ="eyJhbGciOiJIUzI1NiIsImtpZCI6ImIxNWEyYjhmN2E2YjNmNmJjMDhiYzFjNTZhODg0MTBlMTQ2ZDAxZmQiLCJ0eXAiOiJKV1QifQ.eyJlbWFpbCI6ImplcHAzX2RyYWdvbnNsYXllckBmb3J0bm94LnNlIiwibmFtZSI6ImplcHAzIiwicGljdHVyZSI6InVybHRvcGljdHVyZSIsInVzZXJfaWQiOjEsImlhdCI6MTU0ODQxNzY0NywiZXhwIjoxNTQ4NDIxMjQ3fQ.yDPCbMe5ZPqNubrWBoJJytk3DqS5FiEVotirtj3wzNA";
    private static final String INVALID_JWT  ="eyJhbGciOiJIUzI1NiIsImtpZCI6ImIxNWEyYjhmN2E2YjNmNmJjMDhiYzFjNTZhODg0MTBlMTQ2ZDAxZmQiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiamVwcDMiLCJwaWN0dXJlIjoidXJsdG9waWN0dXJlIiwidXNlcl9pZCI6MSwiaWF0IjoxNTQ4NDE3NjQ3LCJleHAiOjE1NDg0MjEyNDd9.T0GzQRUZyncGZj9MYShgvFVV0GJXH1jDv0V2WU7n9OU";

    private OpenIdValidator openIdValidator = new OpenIdValidator();


    @Test
    public void shouldValidateSimpleJWT() {

        ImmutableOpenIdToken token = openIdValidator.validate(SIMPLE_JWT);

       assertEquals("jepp3",token.name);
       assertEquals("jepp3_dragonslayer@fortnox.se",token.email);
       assertEquals("urltopicture",token.picture);

    }

    @Test
    public void shouldThrowWebExceptionIfJWTMissesImportantInformation() {

        try {
           openIdValidator.validate(INVALID_JWT);
            fail("should have thrown " + WebException.class.getName());
        } catch (WebException webException) {
            assertEquals(HttpResponseStatus.UNAUTHORIZED, webException.getStatus());
        }
    }

    @Test
    public void shouldThrowWebExceptionIfJWTCannotBeParsed() {
        try {
            openIdValidator.validate("brutalInvalidJson");
            fail("should have thrown " + WebException.class.getName());
        } catch (WebException webException) {
            assertEquals(HttpResponseStatus.UNAUTHORIZED, webException.getStatus());
        }
    }
}