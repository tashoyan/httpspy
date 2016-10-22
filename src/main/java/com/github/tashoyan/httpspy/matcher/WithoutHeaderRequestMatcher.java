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

import com.github.tashoyan.httpspy.HttpRequest;
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
