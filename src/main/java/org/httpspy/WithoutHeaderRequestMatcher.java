package org.httpspy;

import java.util.List;
import java.util.Map;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher to verify that a request does have the specified header.
 * <p>
 * <b>Concurrency notes.</b> This class is immutable and thread safe.
 */
@Immutable
@ThreadSafe
public class WithoutHeaderRequestMatcher extends TypeSafeMatcher<HttpRequest> {

    private final String headerName;

    /**
     * Creates new matcher.
     * 
     * @param headerName Header name. If a request has a header with this name,
     * then it does not match.
     * @throws NullPointerException headerName is null.
     * @throws IllegalArgumentException headerName is empty or blank.
     */
    public WithoutHeaderRequestMatcher(String headerName) {
        Validate.notBlank(headerName, "headerName must not be blank");
        this.headerName = headerName;
    }

    @Override
    public boolean matchesSafely(HttpRequest httpRequest) {
        Map<String, List<String>> actualHeaders = httpRequest.getHeaders();
        if (MapUtils.isEmpty(actualHeaders)) {
            return true;
        }
        return !actualHeaders.containsKey(headerName);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("[without header : ");
        description.appendValue(headerName);
        description.appendText("]");
    }

    @Override
    public void describeMismatchSafely(HttpRequest httpRequest, Description description) {
        Map<String, List<String>> actualHeaders = httpRequest.getHeaders();
        List<String> headerValues = actualHeaders.get(headerName);
        description.appendText("was ").appendText(headerName).appendText(": ")
                .appendValue(headerValues);
    }
}
