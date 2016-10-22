/*
 * Copyright 2016 Arseniy Tashoyan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.tashoyan.httpspy.matcher;

import java.io.IOException;
import java.util.Objects;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.xml.sax.SAXException;

/**
 * Matcher to verify that a string equals to XML value.
 * <p>
 * <b>Concurrency notes.</b> This class is immutable and thread safe.
 */
@Immutable
@ThreadSafe
public class XmlEqualMatcher extends TypeSafeMatcher<String> {

    private final String expectedValue;
    static {
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalize(true);
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setCompareUnmatched(true);
    }

    /**
     * * Creates new matcher.
     * 
     * @param expectedValue Expected XML value.
     */
    public XmlEqualMatcher(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    @Override
    public boolean matchesSafely(String actualValue) {
        if (StringUtils.isEmpty(expectedValue)) {
            return Objects.equals(expectedValue, actualValue);
        }
        try {
            Diff diff = new Diff(expectedValue, actualValue);
            return diff.similar();
        } catch (SAXException | IOException e) {
            throw new RuntimeException(
                    "Cannot calculate diff between expected value: "
                            + expectedValue + " and actual value: " + actualValue, e);
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("[equals XML : ");
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
            Diff diff = new Diff(expectedValue, actualValue);
            description.appendText("was ").appendText(diff.toString());
        } catch (SAXException | IOException e) {
            throw new RuntimeException(
                    "Cannot calculate diff between expected value: "
                            + expectedValue + " and actual value: " + actualValue, e);
        }
    }
}
