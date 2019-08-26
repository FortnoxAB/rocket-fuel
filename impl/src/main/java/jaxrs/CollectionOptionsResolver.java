package jaxrs;

import rx.Observable;
import se.fortnox.reactivewizard.CollectionOptions;
import se.fortnox.reactivewizard.db.paging.CollectionOptionsWithResult;
import se.fortnox.reactivewizard.jaxrs.JaxRsRequest;
import se.fortnox.reactivewizard.jaxrs.params.ParamResolver;

import static rx.Observable.just;

public class CollectionOptionsResolver implements ParamResolver<CollectionOptions> {

	@Override
	public Observable<CollectionOptions> resolve(JaxRsRequest request) {
		return just(new CollectionOptionsWithResult(getQueryParamAsInteger(request, "limit"),
				getQueryParamAsInteger(request, "offset"),
				request.getQueryParam("sortby"),
				getQueryParamAsSortOrder(request, "order")));
	}

	private CollectionOptions.SortOrder getQueryParamAsSortOrder(JaxRsRequest request, String key) {
		String order = request.getQueryParam(key);
		if (order != null) {
			return CollectionOptions.SortOrder.valueOf(order.toUpperCase());
		} else {
			return null;
		}
	}

	private Integer getQueryParamAsInteger(JaxRsRequest request, String key) {
		String val = request.getQueryParam(key);
		if (val == null) {
			return null;
		}
		try {
			return Integer.valueOf(val);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
