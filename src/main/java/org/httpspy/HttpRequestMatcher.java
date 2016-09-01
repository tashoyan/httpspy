package org.httpspy;

import java.util.function.Function;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher to verify a value of some attribute of {@link HttpRequest actually
 * received requests}.
 * <p>
 * <b>Concurrency notes.</b> This class is immutable and thread safe.
 */
@Immutable
@ThreadSafe
public class HttpRequestMatcher extends TypeSafeMatcher<HttpRequest> {

    private final Function<HttpRequest, Object> attributeProvider;

    private final ValueExpectation valueExpectation;

    private final String name;

    /**
     * Creates new matcher.
     * 
     * @param attributeProvider Provider of a request attribute, takes the
     * request on input.
     * @param valueExpectation Expected attribute value.
     * @param name Name of the matcher to be used in a mismatch description.
     * @throws IllegalArgumentException attributeProvider is null.
     * @throws IllegalArgumentException valueExpectation is null.
     * @throws IllegalArgumentException name is null or empty or blank.
     */
    protected HttpRequestMatcher(Function<HttpRequest, Object> attributeProvider,
            ValueExpectation valueExpectation, String name) {
        Validate.notNull(attributeProvider, "attributeProvider must not be null");
        Validate.notNull(valueExpectation, "valueExpectation must not be null");
        Validate.isTrue(StringUtils.isNotBlank(name), "headerName must not be blank");
        this.attributeProvider = attributeProvider;
        this.valueExpectation = valueExpectation;
        this.name = name;
    }

    @Override
    public boolean matchesSafely(HttpRequest httpRequest) {
        return valueExpectation.getMatcher().matches(
                attributeProvider.apply(httpRequest));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("["
                + name + " : ");
        description.appendDescriptionOf(valueExpectation.getMatcher());
        description.appendText("]");
    }

    @Override
    public void describeMismatchSafely(HttpRequest httpRequest, Description description) {
        description.appendText("was ").appendValue(
                attributeProvider.apply(httpRequest));
    }
}
