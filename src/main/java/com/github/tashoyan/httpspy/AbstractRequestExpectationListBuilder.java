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
package com.github.tashoyan.httpspy;

import java.util.ArrayList;
import java.util.List;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Abstract class that has implementations for almost all methods of
 * {@link RequestExpectationListBuilder}.
 * <p>
 * User has nothing to implement but {@link #build() } method.
 */
@NotThreadSafe
public abstract class AbstractRequestExpectationListBuilder
        implements RequestExpectationListBuilder {

    private final List<RequestExpectation> requestExpectations = new ArrayList<>(
            CamelJettyHttpSpy.DEFAULT_REQUESTS_NUMBER);

    private final List<HttpResponse> responses = new ArrayList<>(
            CamelJettyHttpSpy.DEFAULT_REQUESTS_NUMBER);

    @Override
    public List<RequestExpectation> getRequestExpectations() {
        return requestExpectations;
    }

    @Override
    public List<HttpResponse> getResponses() {
        return responses;
    }

    @Override
    public void expect(RequestExpectationBuilder requestExpectationBuilder) {
        Validate.notNull(requestExpectationBuilder,
                "requestExpectationBuilder must not be null");
        RequestExpectation requestExpectation = requestExpectationBuilder.build();
        ResponseBuilder responseBuilder =
                requestExpectationBuilder.getResponseBuilder();
        HttpResponse response = responseBuilder.build();
        requestExpectations.add(requestExpectation);
        responses.add(response);
    }

    /**
     * Expect the same request multiple times.
     * <p>
     * A convenience method instead of calling
     * {@link #expect(RequestExpectationBuilder) } multiple times.
     * 
     * @param times Number of sequential requests with the same expectation.
     * @param requestExpectationBuilder Request expectation builder.
     * @throws NullPointerException requestExpectationBuilder is null.
     * @throws IllegalArgumentException times is not positive.
     */
    public void expect(int times, RequestExpectationBuilder requestExpectationBuilder) {
        Validate.isTrue(times > 0, "times must be positive");
        Validate.notNull(requestExpectationBuilder,
                "requestExpectationBuilder must not be null");
        RequestExpectation requestExpectation = requestExpectationBuilder.build();
        ResponseBuilder responseBuilder =
                requestExpectationBuilder.getResponseBuilder();
        HttpResponse response = responseBuilder.build();
        for (int i = 0; i < times; i++) {
            requestExpectations.add(requestExpectation);
            responses.add(response);
        }
    }

    @Override
    public RequestExpectationBuilder request() {
        return new DefaultRequestExpectationBuilder();
    }

    @Override
    public ResponseBuilder response() {
        return new DefaultResponseBuilder();
    }

    /**
     * Creates {equal to XML} value expectation.
     * 
     * @param value Expected XML value. Null or empty values is allowed.
     * @return Value expectation object that means equality to some XML.
     */
    public static ValueExpectation equalToXml(String value) {
        return () -> new XmlEqualMatcher(value);
    }

    /**
     * Creates {equal to JSON} value expectation.
     * 
     * @param value Expected JSON value. Null or empty values is allowed.
     * @return Value expectation object that means equality to some JSON.
     */
    public static ValueExpectation equalToJson(String value) {
        return () -> new JsonEqualMatcher(value);
    }

    /**
     * Creates {@code equal to} value expectations.
     * 
     * @param value Expected value.
     * @return Value expectation object that means equality to the given value.
     */
    public static ValueExpectation equalTo(String value) {
        return () -> CoreMatchers.equalTo(value);
    }

    /**
     * Creates {@code equal to ignoring letter case} value expectations.
     * 
     * @param value Expected value.
     * @return Value expectation object that means equality to the given value
     * ignoring letter case.
     */
    public static ValueExpectation equalToIgnoreCase(String value) {
        return () -> new TypeSafeMatcher<String>() {

            @Override
            public boolean matchesSafely(String actualValue) {
                return StringUtils.equalsIgnoreCase(value, actualValue);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(value
                        + " (ignoring case)");
            }
        };
    }

    /**
     * Creates {@code matches} value expectation.
     * 
     * @param valueMatcher Expected matching value.
     * @return Value expectation object that means matching to the given value.
     */
    public static ValueExpectation matching(Matcher<String> valueMatcher) {
        return () -> valueMatcher;
    }
}
