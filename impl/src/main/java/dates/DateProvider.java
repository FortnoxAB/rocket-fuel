package dates;

import java.time.OffsetDateTime;

/**
 * Provides an abstraction for retrieving various date formats.
 *
 * Useful during testing.
 */
public interface DateProvider {

	OffsetDateTime getOffsetDateTime();
}
