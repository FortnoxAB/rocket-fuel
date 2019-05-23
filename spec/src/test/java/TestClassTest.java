import impl.TestClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestClassTest {
    @Test
    public void testHello() {
        TestClass testClass = new TestClass();
        assertThat(testClass.sayHello()).isEqualTo("hello");
    }
}
