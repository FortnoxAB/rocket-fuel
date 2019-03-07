package slack;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import se.fortnox.reactivewizard.binding.AutoBindModule;
import se.fortnox.reactivewizard.binding.scanners.SlackMessageHandlerScanner;

@Singleton
public class SlackModule implements AutoBindModule {

    private final SlackMessageHandlerScanner slackMessageHandlerScanner;

    @Inject
    public SlackModule(SlackMessageHandlerScanner slackMessageHandlerScanner) {

        this.slackMessageHandlerScanner = slackMessageHandlerScanner;
    }

    @Override
    public void configure(Binder binder) {

        Multibinder<SlackMessageHandler> slackMessageHandlerMultibinder = Multibinder.newSetBinder(binder,
            TypeLiteral.get(SlackMessageHandler.class));

        slackMessageHandlerScanner.getMessageHandlers().forEach(aClass -> slackMessageHandlerMultibinder.addBinding().to(aClass));

        binder.bind(SlackRTMClient.class).asEagerSingleton();
    }
}
