package dates;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Singleton
public class DateProviderImpl implements DateProvider {
	private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Z");

	@Inject
	public DateProviderImpl() {
        // used by guice injector
	}

	@Override
	public OffsetDateTime getOffsetDateTime() {
		return OffsetDateTime.now();
	}

	public ZoneId getDefaultZone() {
		return DEFAULT_ZONE_ID;
	}
}
