package org.httpspy;

import java.util.List;
import java.util.Map;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher to verify that a request has a header with a specified value.
 * <p>
 * <b>Concurrency notes.</b> This class is immutable and thread safe.
 */
@Immutable
@ThreadSafe
public class HttpRequestHeaderMatcher extends TypeSafeMatcher<HttpRequest> {

    private final String headerName;

    private final ValueExpectation valueExpectation;

    /**
     * Creates new matcher.
     * <p>
     * 
     * @param headerName Header name.
     * @param valueExpectation Expected header value.
     * @throws IllegalArgumentException headerName is null or empty or blank.
     * @throws IllegalArgumentException valueExpectation is null.
     */
    public HttpRequestHeaderMatcher(String headerName,
            ValueExpectation valueExpectation) {
        Validate.isTrue(StringUtils.isNotBlank(headerName),
                "headerName must not be blank");
        Validate.notNull(valueExpectation, "valueExpectation must not be null");
        this.headerName = headerName;
        this.valueExpectation = valueExpectation;
    }

    @Override
    public boolean matchesSafely(HttpRequest httpRequest) {
        Map<String, List<String>> actualHeaders = httpRequest.getHeaders();
        List<String> headerValues = actualHeaders.get(headerName);
        if (CollectionUtils.isEmpty(headerValues)) {
            return false;
        }
        if (headerValues.stream().anyMatch(
                headerValue -> valueExpectation.getMatcher().matches(headerValue))) {
            return true;
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("[header "
                + headerName + " : ");
        description.appendDescriptionOf(valueExpectation.getMatcher());
        description.appendText("]");
    }

    @Override
    public void describeMismatchSafely(HttpRequest httpRequest, Description description) {
        description.appendText("was ");
        Map<String, List<String>> actualHeaders = httpRequest.getHeaders();
        List<String> headerValues = actualHeaders.get(headerName);
        if (headerValues == null) {
            description.appendText("no such header: "
                    + headerName);
            return;
        }
        description.appendValue(headerValues);
    }
}
