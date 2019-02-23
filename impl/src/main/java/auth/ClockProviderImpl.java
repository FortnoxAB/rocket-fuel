package auth;

import com.auth0.jwt.interfaces.Clock;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Date;

/**
 * Standard implementation fo the ClockProvider interface that returns the current date.
 */
@Singleton
public class ClockProviderImpl implements ClockProvider {

    @Inject
    public ClockProviderImpl() {
        // used by guice
    }

    /**
     * @return the current {@link Date}
     */
    @Override
    public Clock getClock() {
        return Date::new;
    }
}
