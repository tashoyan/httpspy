package org.httpspy;

import java.util.Objects;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

/**
 * Matcher to verify that a string equals to JSON value.
 * <p>
 * <b>Concurrency notes.</b> This class is immutable and thread safe.
 */
@Immutable
@ThreadSafe
public class JsonEqualMatcher extends TypeSafeMatcher<String> {

    private final String expectedValue;

    /**
     * * Creates new matcher.
     * 
     * @param expectedValue Expected JSON value.
     */
    public JsonEqualMatcher(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    @Override
    public boolean matchesSafely(String actualValue) {
        if (StringUtils.isEmpty(expectedValue)) {
            return Objects.equals(expectedValue, actualValue);
        }
        try {
            JSONCompareResult result =
                    JSONCompare.compareJSON(expectedValue, actualValue,
                            JSONCompareMode.NON_EXTENSIBLE);
            return result.passed();
        } catch (JSONException e) {
            throw new RuntimeException("Cannot compare expected value: "
                    + expectedValue + " and actual value: " + actualValue, e);
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("[equals JSON : ");
        description.appendValue(expectedValue);
        description.appendText("]");
    }

    @Override
    public void describeMismatchSafely(String actualValue, Description description) {
        if (StringUtils.isEmpty(expectedValue)) {
            description.appendText("was ").appendText(actualValue);
            return;
        }
        try {
            JSONCompareResult result =
                    JSONCompare.compareJSON(expectedValue, actualValue,
                            JSONCompareMode.NON_EXTENSIBLE);
            description.appendText("was ").appendText(result.getMessage());
        } catch (JSONException e) {
            throw new RuntimeException("Cannot compare expected value: "
                    + expectedValue + " and actual value: " + actualValue, e);
        }
    }
}
