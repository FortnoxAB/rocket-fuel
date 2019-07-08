package util;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssertAlternative;
import org.assertj.core.api.ThrowableTypeAssert;
import rx.Observable;

import java.util.concurrent.Callable;

public class ObservableThrowableAssert<T extends Throwable> extends ThrowableTypeAssert<T> {

    public ObservableThrowableAssert(Class<? extends T> throwableType) {
        super(throwableType);
    }

    public ThrowableAssertAlternative<? extends T> isThrownBy(Callable<Observable<?>> callable) {
        return Assertions.assertThatExceptionOfType(expectedThrowableType)
            .isThrownBy(() -> callable.call().toBlocking().single());
    }
}
