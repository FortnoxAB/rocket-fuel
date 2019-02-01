package impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import se.fortnox.reactivewizard.jaxrs.JaxRsResource;
import se.fortnox.reactivewizard.jaxrs.response.ResultTransformer;
import se.fortnox.reactivewizard.jaxrs.response.ResultTransformerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Singleton
public class ResponseHeadersTransformerFactory implements ResultTransformerFactory, ResponseHeaderHolder {

	private static final long EXPIRATION_MS = 60000;
	private final Cache<Object, Map<String, Object>> storedHeaders;

	@Inject
	public ResponseHeadersTransformerFactory() {
		storedHeaders = CacheBuilder.newBuilder()
			.expireAfterWrite(EXPIRATION_MS, TimeUnit.MILLISECONDS)
			.build();
	}

	@Override
	public <T> ResultTransformer<T> create(JaxRsResource<T> jaxRsResource) {
		return (result, args) -> result.doOnOutput(output -> {

			if (result != null && output != null) {
				Map<String, Object> responseHeaders = storedHeaders.getIfPresent(output);
				storedHeaders.invalidate(output);

				if (responseHeaders != null) {
					responseHeaders.forEach(result::addHeader);
				}
			}
		});
	}

	@Override
	public void addHeaders(Object result, Map<String, Object> responseHeaders) {
		storedHeaders.put(result, responseHeaders);
	}
}
