package se.fortnox.reactivewizard.binding.scanners;

import com.google.inject.Singleton;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import slack.SlackMessageHandler;

import java.util.HashSet;
import java.util.Set;

import static java.lang.reflect.Modifier.isAbstract;

/**
 * Scans classpath for classes implementing the SlackMessageHandler-Interface
 *
 * So creating and invoking the next handler will be as simple as creating the class and implementing the methods.
 */
@Singleton
public class SlackMessageHandlerScanner extends AbstractClassScanner {

    private final Set<Class<? extends SlackMessageHandler>> messageHandlers = new HashSet<>();

    @Override
    public void visit(FastClasspathScanner classpathScanner) {
        classpathScanner.matchClassesImplementing(SlackMessageHandler.class, clazz -> {
            if(!isAbstract(clazz.getModifiers())) {
                messageHandlers.add(clazz);
            }
        });
    }

    public Set<Class<? extends SlackMessageHandler>> getMessageHandlers() {
        return messageHandlers;
    }
}
