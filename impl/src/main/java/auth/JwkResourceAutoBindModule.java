package auth;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import se.fortnox.reactivewizard.binding.AutoBindModule;

public class JwkResourceAutoBindModule implements AutoBindModule {

    @Override
    public void configure(Binder binder) {
        binder.bind(JwkResource.class).toProvider(JwkResourceProvider.class)
                .in(Singleton.class);
    }

}
