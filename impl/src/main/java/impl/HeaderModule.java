package impl;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import se.fortnox.reactivewizard.binding.AutoBindModule;
import se.fortnox.reactivewizard.jaxrs.response.ResultTransformerFactory;

public class HeaderModule implements AutoBindModule {
	@Override
	public void configure(Binder binder) {
		Multibinder<ResultTransformerFactory> responseTransformerFactories = Multibinder.newSetBinder(binder,
			TypeLiteral.get(ResultTransformerFactory.class));
		responseTransformerFactories.addBinding().to(ResponseHeadersTransformerFactory.class);

	}
}
