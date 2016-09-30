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
package org.httpspy;

import java.util.List;
import java.util.Map;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
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
     * @throws NullPointerException headerName is null.
     * @throws IllegalArgumentException headerName is empty or blank.
     * @throws NullPointerException valueExpectation is null.
     */
    public HttpRequestHeaderMatcher(String headerName,
            ValueExpectation valueExpectation) {
        Validate.notBlank(headerName, "headerName must not be blank");
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
