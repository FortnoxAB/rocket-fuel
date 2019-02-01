package auth;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import se.fortnox.reactivewizard.binding.AutoBindModule;
import se.fortnox.reactivewizard.jaxrs.params.ParamResolverFactory;

/**
 *  Makes sure that the authResolver will be invoked a method having a Auth in
 *  the signature is supposed to be called.
 *
 */
public class AuthBindModule implements AutoBindModule {

    @Override
    public void configure(Binder binder) {
        Multibinder.newSetBinder(binder, TypeLiteral.get(ParamResolverFactory.class))
                .addBinding()
                .to(AuthResolverFactory.class);
        binder.bind(AuthResolver.class).to(AuthResolverImpl.class);
    }

    @Override
    public Integer getPrio() {
        return 200;
    }
}
