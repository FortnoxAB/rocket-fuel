package auth.openid;

/**
 * Contains all used claims from the openId jwt
 */
public abstract class OpenIdClaims {

    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String PICTURE = "picture";

    private OpenIdClaims() {}
}
