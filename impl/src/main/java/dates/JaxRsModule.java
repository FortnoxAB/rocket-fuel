package dates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.inject.Binder;
import se.fortnox.reactivewizard.binding.AutoBindModule;
import se.fortnox.reactivewizard.binding.scanners.InjectAnnotatedScanner;

import javax.inject.Inject;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

/**
 * Workaround for framework bug. Should be removed once fixed.
 */
public class JaxRsModule implements AutoBindModule {

	private final InjectAnnotatedScanner injectAnnotatedScanner;

	@Inject
	public JaxRsModule(InjectAnnotatedScanner injectAnnotatedScanner) {
		this.injectAnnotatedScanner = injectAnnotatedScanner;
	}

	@Override
	public void configure(Binder binder) {
		binder.bind(DateFormat.class).toProvider(RWDateFormat::new);
		binder.bind(ObjectMapper.class).toInstance(new ObjectMapper()
			.findAndRegisterModules()
			.registerModule(createJavaTimeModule())
			.configure(WRITE_DATES_AS_TIMESTAMPS, false)
			.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
			.configure(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
			.setDateFormat(new RWDateFormat()));
    }

	private static JavaTimeModule createJavaTimeModule() {
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		final DateTimeFormatter dateTimeFormatterAllowingSpace = new DateTimeFormatterBuilder().parseCaseInsensitive()
			.append(ISO_LOCAL_DATE)
			.optionalStart()
			.appendPattern("[[ ]['T']]")
			.append(ISO_LOCAL_TIME)
			.optionalEnd()
			.toFormatter();
		javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatterAllowingSpace));
		javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(new DateTimeFormatterBuilder()
			.optionalStart()
			.append(dateTimeFormatterAllowingSpace)
			.optionalEnd()
			.optionalStart()
			.append(DateTimeFormatter.ofPattern("yyyyMMdd"))
			.optionalEnd()
			.toFormatter()));
		return javaTimeModule;
	}
}
