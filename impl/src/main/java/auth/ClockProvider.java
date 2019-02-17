package auth;

import com.auth0.jwt.interfaces.Clock;

/**
 * Provides a clock for {@link com.auth0.jwt.JWTVerifier}
 */
public interface ClockProvider {
    /**
     * Returns the current time that the {@link com.auth0.jwt.JWTVerifier} will use
     * while verifying tokens.
     *
     * @return a clock instance
     */
    Clock getClock();
}
