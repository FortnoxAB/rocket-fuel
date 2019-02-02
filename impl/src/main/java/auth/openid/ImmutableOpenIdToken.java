package auth.openid;

/**
 * Represents a openId.
 */
public class ImmutableOpenIdToken {

    public final String name;
    public final String email;
    public final String picture;

    public ImmutableOpenIdToken(String name, String email, String picture) {
        this.name = name;
        this.email = email;
        this.picture = picture;
    }
}
