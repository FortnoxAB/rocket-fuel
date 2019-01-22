package api;

import java.time.OffsetDateTime;

/**
 * An specification of an authorized user.
 */
 public abstract class Auth {

    private long userId;
    private String email;
    private OffsetDateTime expires;

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setExpires(OffsetDateTime expires) {
        this.expires = expires;
    }

    public OffsetDateTime getExpires() {
        return expires;
    }
}