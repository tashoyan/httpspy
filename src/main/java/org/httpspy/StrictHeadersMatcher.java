package org.httpspy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.Validate;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher to verify that a request has only headers with specified names.
 * <p>
 * <b>Concurrency notes.</b> This class is immutable and thread safe.
 */
@Immutable
@ThreadSafe
public class StrictHeadersMatcher extends TypeSafeMatcher<HttpRequest> {

    private final Set<String> specifiedHeaders;

    /**
     * Creates new matcher.
     * 
     * @param specifiedHeaders Specified set of header names. If a request has a
     * header not from this set, then it does not match. Empty set means only
     * requests without headers match.
     * @throws IllegalArgumentException specifiedHeaders is null.
     */
    public StrictHeadersMatcher(Set<String> specifiedHeaders) {
        Validate.notNull(specifiedHeaders, "specifiedHeaders must not be null");
        this.specifiedHeaders = Collections.unmodifiableSet(specifiedHeaders);
    }

    @Override
    public boolean matchesSafely(HttpRequest httpRequest) {
        Map<String, List<String>> actualHeaders = httpRequest.getHeaders();
        if (MapUtils.isEmpty(actualHeaders)) {
            return specifiedHeaders.isEmpty();
        }
        return specifiedHeaders.size() == actualHeaders.keySet().size()
                && specifiedHeaders.containsAll(actualHeaders.keySet());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("[strict headers : ");
        description.appendValue(specifiedHeaders);
        description.appendText("]");
    }

    @Override
    public void describeMismatchSafely(HttpRequest httpRequest, Description description) {
        Map<String, List<String>> actualHeaders = httpRequest.getHeaders();
        description.appendText("was ").appendValue(actualHeaders);
    }
}
