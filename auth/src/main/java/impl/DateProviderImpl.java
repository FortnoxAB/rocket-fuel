package impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.OffsetDateTime;

@Singleton
public class DateProviderImpl implements DateProvider {

	@Inject
	public DateProviderImpl() {

	}

	@Override
	public OffsetDateTime getOffsetDateTime() {
		return OffsetDateTime.now();
	}
}
