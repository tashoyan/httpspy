package org.httpspy;

import net.jcip.annotations.ThreadSafe;
import org.hamcrest.Matcher;

/**
 * Expectation for a value of some string attribute.
 * <p>
 * <b>Concurrency notes.</b> User creates an object of this class when setting
 * expectations in the thread that runs the test - typically {@code main}
 * thread. During the test execution, another thread reads data from the
 * expectation object, but does not modify the object. Finally, the original
 * thread reads the expectation object during verification. Therefore an
 * implementation should be thread safe.
 */
@ThreadSafe
@FunctionalInterface
public interface ValueExpectation {

    /**
     * Gets a matcher that verifies attribute value.
     * 
     * @return Matcher to verify attribute value.
     */
    Matcher<String> getMatcher();
}
