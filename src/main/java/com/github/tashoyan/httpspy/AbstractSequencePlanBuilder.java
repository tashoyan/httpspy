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
import org.apache.commons.lang3.Validate;

/**
 * Builder for {@link SequencePlan}.
 */
public abstract class AbstractSequencePlanBuilder
        extends AbstractTestPlanBuilder<SequencePlan> {

    private final List<RequestExpectation> requestExpectations = new ArrayList<>(
            DEFAULT_REQUESTS_NUMBER);

    private final List<HttpResponse> responses = new ArrayList<>(
            DEFAULT_REQUESTS_NUMBER);

    /**
     * {@inheritDoc }
     * <p>
     * The method adds new request expectation as specified by the argument to
     * the end of expectations list. Also the method adds the response for the
     * actual request to the end of responses list.
     * 
     * @param requestExpectationBuilder Request expectation builder.
     * @throws NullPointerException requestExpectationBuilder is null.
     */
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
     * Expect the same request multiple times in sequence.
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
    public SequencePlan build() {
        this.compose();
        return new SequencePlan(requestExpectations, responses);
    }

    /**
     * Composes the test plan.
     * <p>
     * User has to implement this method when preparing a test plan. User can
     * invoke {@link #expect } inside his implementation of this method to
     * specify request expectations.
     */
    public abstract void compose();
}
