package org.httpspy;

import net.jcip.annotations.ThreadSafe;
import org.hamcrest.Matcher;

/**
 * Request expectation.
 * <p>
 * Provides expectations for a request received by {@link HttpServerMock}.
 * During verification, mock server compares every actual request received with
 * its expectation. In addition, expectation may declare hoe mock server should
 * response on the request.
 * <p>
 * <b>Concurrency notes.</b> User creates an object of this class when setting
 * expectations in the thread that runs the test - typically {@code main}
 * thread. During the test execution, another thread reads data from the
 * expectation object, but does not modify the object. Finally, the original
 * thread reads the expectation object during verification. Therefore an
 * implementation should be thread safe.
 * 
 * @see HttpServerMock#verify()
 */
@ThreadSafe
public interface RequestExpectation {

    /**
     * Gets matcher to check an actual request against this expectation.
     * 
     * @return Matcher for actual requests. Should never return null.
     */
    Matcher<HttpRequest> getRequestMatcher();
}
