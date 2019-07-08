package util;

import rx.Observable;

import java.util.List;

public class ObservableAssertions {

    public static <T> ObservableAssert<T> assertThat(Observable<T> actual) {
        return new ObservableAssert<>(actual);
    }

    public static <E> ObservableListAssert<E> assertThatList(Observable<List<E>> actual) {
        return new ObservableListAssert<>(actual);
    }

    public static <T extends Throwable> ObservableThrowableAssert<T> assertThatExceptionOfType(final Class<? extends T> exceptionType) {
        return new ObservableThrowableAssert<T>(exceptionType);
    }
}
