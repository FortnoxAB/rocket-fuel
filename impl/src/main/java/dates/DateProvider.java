package dates;

import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Provides an abstraction for retrieving various date formats.
 *
 * Useful during testing.
 */
public interface DateProvider {

	OffsetDateTime getOffsetDateTime();

	ZoneId getDefaultZone();
}
