package dates;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Singleton
public class DateProviderImpl implements DateProvider {

	@Inject
	public DateProviderImpl() {

	}

	@Override
	public OffsetDateTime getOffsetDateTime() {
		return OffsetDateTime.now();
	}


	public ZoneId getDefaultZone() {
		return ZoneId.of("Z");
	}
}
