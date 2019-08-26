package util;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import rx.Observable;

import java.util.List;

/**
 * Assertion for an Observable of a List.
 * @param <E> type of element in List
 */
public class ObservableListAssert<E> extends AbstractAssert<ObservableListAssert<E>, Observable<List<E>>> {

    public ObservableListAssert(Observable<List<E>> actual) {
        super(actual, ObservableListAssert.class);
        isNotNull();
    }

    /**
     * Verifies that the Observable receives a single non-empty event and no errors.
     *
     * @return a new assertion object whose object under test is the received List of E
     */
    public ListAssert<E> hasExactlyOne() {
        List<List<E>> values = actual.test().awaitTerminalEvent().assertNoErrors().getOnNextEvents();
        Assertions.assertThat(values)
            .hasSize(1);
        return Assertions.assertThat(values.get(0));
    }
}
