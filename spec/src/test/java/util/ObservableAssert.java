package util;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import rx.Observable;

import java.util.List;

/**
 * Assertion for an Observable.
 * @param <T>
 */
public class ObservableAssert<T> extends AbstractAssert<ObservableAssert<T>, Observable<T>> {

    public ObservableAssert(Observable<T> actual) {
        super(actual, ObservableAssert.class);
        isNotNull();
    }

    /**
     * Verifies that the Observable receives a single event and no errors.
     * @return a new assertion object whose object under test is the received event
     */
    public ObjectAssert<T> hasExactlyOne() {
        List<T> values = actual.test().awaitTerminalEvent().assertNoErrors().getOnNextEvents();
        Assertions.assertThat(values)
            .hasSize(1);
        return Assertions.assertThat(values.get(0));
    }

    public void isEmpty() {
        actual.test().awaitTerminalEvent().assertNoValues();
    }
}
