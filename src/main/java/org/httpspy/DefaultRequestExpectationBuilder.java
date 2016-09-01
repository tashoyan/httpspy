package org.httpspy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;

/**
 * Default implementation of {@link RequestExpectationBuilder}.
 * <p>
 * TODO Test.
 */
@NotThreadSafe
public class DefaultRequestExpectationBuilder implements RequestExpectationBuilder {

    private static final int USUAL_SPECIFIED_MATCHERS_NUMBER = 10;

    private static final int USUAL_SPECIFIED_HEADERS_NUMBER = 10;

    private final List<Matcher<? super HttpRequest>> requestMatchers;

    private final Set<String> specifiedHeaders;

    private boolean isStrictHeaders;

    private ResponseBuilder responseBuilder;

    /**
     * Creates new builder instance.
     * <p>
     * New instance matches any request and provides default response from
     * {@link DefaultResponseBuilder}.
     * <p>
     * End user is not supposed to call this constructor and instead should call
     * {@link AbstractRequestExpectationListBuilder#request() } when implementing
     * {@link AbstractRequestExpectationListBuilder#build() }.
     */
    protected DefaultRequestExpectationBuilder() {
        requestMatchers = new ArrayList<>(USUAL_SPECIFIED_MATCHERS_NUMBER);
        specifiedHeaders = new HashSet<>(USUAL_SPECIFIED_HEADERS_NUMBER);
        responseBuilder = new DefaultResponseBuilder();
    }

    @Override
    public RequestExpectation build() {
        if (isStrictHeaders) {
            requestMatchers.add(new StrictHeadersMatcher(specifiedHeaders));
        }
        return new DefaultRequestExpectation(CoreMatchers.allOf(requestMatchers));
    }

    @Override
    public ResponseBuilder getResponseBuilder() {
        return responseBuilder;
    }

    /**
     * Sets expectation on the value of a request attribute.
     * <p>
     * Adds a {@link Matcher} that verifies the given request attribute.
     * 
     * @param attributeProvider Provider of a request attribute, takes the
     * request on input.
     * @param matcherName Name of the matcher to be used in a mismatch
     * description.
     * @param valueExpectation Expected attribute value.
     * @return This object.
     */
    protected RequestExpectationBuilder withRequestAttribute(
            Function<HttpRequest, Object> attributeProvider, String matcherName,
            ValueExpectation valueExpectation) {
        requestMatchers.add(new HttpRequestMatcher(attributeProvider,
                valueExpectation, matcherName));
        return this;
    }

    @Override
    public RequestExpectationBuilder withMethod(ValueExpectation valueExpectation) {
        assertValueExpectationNotNull(valueExpectation);
        return withRequestAttribute(httpRequest -> httpRequest.getMethod(), "method",
                valueExpectation);
    }

    @Override
    public RequestExpectationBuilder withPath(ValueExpectation valueExpectation) {
        assertValueExpectationNotNull(valueExpectation);
        return withRequestAttribute(httpRequest -> httpRequest.getPath(), "path",
                valueExpectation);
    }

    @Override
    public RequestExpectationBuilder withBody(ValueExpectation valueExpectation) {
        assertValueExpectationNotNull(valueExpectation);
        return withRequestAttribute(httpRequest -> httpRequest.getBody(), "body",
                valueExpectation);
    }

    @Override
    public RequestExpectationBuilder withHeader(String headerName) {
        assertHeaderNameNotBlank(headerName);
        specifiedHeaders.add(headerName);
        requestMatchers.add(new HttpRequestHeaderMatcher(headerName,
                () -> CoreMatchers.any(String.class)));
        return this;
    }

    @Override
    public RequestExpectationBuilder withHeader(String headerName,
            ValueExpectation valueExpectation) {
        assertValueExpectationNotNull(valueExpectation);
        assertHeaderNameNotBlank(headerName);
        specifiedHeaders.add(headerName);
        requestMatchers
                .add(new HttpRequestHeaderMatcher(headerName, valueExpectation));
        return this;
    }

    @Override
    public RequestExpectationBuilder withHeader(String headerName, int valueIndex,
            ValueExpectation valueExpectation) {
        assertHeaderNameNotBlank(headerName);
        Validate.isTrue(valueIndex >= 0, "valueIndex must be >= 0: "
                + valueIndex);
        assertValueExpectationNotNull(valueExpectation);
        specifiedHeaders.add(headerName);
        return withRequestAttribute(
                httpRequest -> httpRequest.getHeaderValues(headerName).get(valueIndex),
                "header "
                        + headerName + " - value index " + valueIndex,
                valueExpectation);
    }

    @Override
    public RequestExpectationBuilder withoutHeader(String headerName) {
        assertHeaderNameNotBlank(headerName);
        requestMatchers.add(new WithoutHeaderRequestMatcher(headerName));
        return this;
    }

    @Override
    public RequestExpectationBuilder withStrictHeaders() {
        isStrictHeaders = true;
        return this;
    }

    @Override
    public RequestExpectationBuilder andResponse(ResponseBuilder responseBuilder) {
        Validate.notNull(responseBuilder, "responseBuilder must not be null");
        this.responseBuilder = responseBuilder;
        return this;
    }

    private void assertValueExpectationNotNull(ValueExpectation valueExpectation) {
        Validate.notNull(valueExpectation, "valueExpectation must not be null");
    }

    private void assertHeaderNameNotBlank(String headerName) {
        Validate.isTrue(StringUtils.isNotBlank(headerName),
                "headerName must not be blank");
    }
}
