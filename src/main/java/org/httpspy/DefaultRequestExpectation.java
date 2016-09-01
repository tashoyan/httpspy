package org.httpspy;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang.Validate;
import org.hamcrest.Matcher;

/**
 * Default implementation of {@link RequestExpectation}.
 */
@Immutable
@ThreadSafe
public class DefaultRequestExpectation implements RequestExpectation {

    private final Matcher<HttpRequest> requestMatcher;

    /**
     * Constructs new instance of request expectation.
     * <p>
     * End user is not supposed to call this constructor and instead should call
     * {@link RequestExpectationListBuilder#expect }.
     * 
     * @param requestMatcher Request matcher to check an actual request against
     * this expectation.
     * @throws IllegalArgumentException requestMatcher is null.
     */
    protected DefaultRequestExpectation(Matcher<HttpRequest> requestMatcher) {
        Validate.notNull(requestMatcher, "requestMatcher must not be null");
        this.requestMatcher = requestMatcher;
    }

    @Override
    public Matcher<HttpRequest> getRequestMatcher() {
        return requestMatcher;
    }
}
