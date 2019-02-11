package se.fortnox.reactivewizard.binding.scanners;

import com.google.inject.Singleton;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ImplementingClassMatchProcessor;
import slack.SlackMessageHandler;

import java.util.HashSet;
import java.util.Set;

@Singleton
public class SlackMessageHandlerScanner extends AbstractClassScanner {

    private final Set<Class<? extends SlackMessageHandler>> messageHandlers = new HashSet<>();

    @Override
    public void visit(FastClasspathScanner classpathScanner) {
        classpathScanner.matchClassesImplementing(SlackMessageHandler.class, (ImplementingClassMatchProcessor<SlackMessageHandler>)messageHandlers::add);
    }

    public Set<Class<? extends SlackMessageHandler>> getMessageHandlers() {
        return messageHandlers;
    }
}
