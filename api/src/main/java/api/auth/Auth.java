package api.auth;

import java.time.OffsetDateTime;

/**
 * An specification of an authorized user.
 */
 public class Auth {

    private long userId;
    private String email;
    private OffsetDateTime expires;
    private String name;
    private String picture;

    public Auth() {

    }

    public Auth(long userId) {
        this.userId = userId;
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getPicture() {
        return picture;
    }
}
