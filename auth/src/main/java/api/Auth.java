package api;
/**
 * An specification of an authorized user.
 */
 public abstract class Auth {

    private final long userId;

    public Auth(Long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }
}