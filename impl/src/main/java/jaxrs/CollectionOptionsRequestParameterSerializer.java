package jaxrs;

import se.fortnox.reactivewizard.CollectionOptions;
import se.fortnox.reactivewizard.client.RequestBuilder;
import se.fortnox.reactivewizard.client.RequestParameterSerializer;

import javax.inject.Singleton;

@Singleton
public class CollectionOptionsRequestParameterSerializer implements RequestParameterSerializer<CollectionOptions> {
    @Override
    public void addParameter(CollectionOptions param, RequestBuilder request) {
        if (param == null) {
            return;
        }
        if (param.getLimit() != null) {
            request.addQueryParam("limit", String.valueOf(param.getLimit()));
        }
        if (param.getOffset() != null) {
            request.addQueryParam("offset", String.valueOf(param.getOffset()));
        }
        if (param.getSortBy() != null) {
            request.addQueryParam("sortby", param.getSortBy());
        }
        if (param.getOrder() != null) {
            request.addQueryParam("order", param.getOrder().name());
        }
    }
}
