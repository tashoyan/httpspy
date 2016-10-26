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

import java.util.Deque;
import java.util.LinkedList;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang3.Validate;

/**
 * Builder for {@link StubPlan}.
 * <p>
 * This builder reverses the order in which user adds request expectations and
 * responses. In other words, {@link StubPlan} will start to evaluate an actual
 * request against the collection of request expectations <b>from the latest</b>
 * expectation in the collection.
 */
@NotThreadSafe
public abstract class AbstractStubPlanBuilder
        extends AbstractTestPlanBuilder<StubPlan> {

    private final Deque<RequestExpectation> requestExpectations = new LinkedList<>();

    private final Deque<HttpResponse> responses = new LinkedList<>();

    @Override
    public void expect(RequestExpectationBuilder requestExpectationBuilder) {
        Validate.notNull(requestExpectationBuilder,
                "requestExpectationBuilder must not be null");
        RequestExpectation requestExpectation = requestExpectationBuilder.build();
        ResponseBuilder responseBuilder =
                requestExpectationBuilder.getResponseBuilder();
        HttpResponse response = responseBuilder.build();
        requestExpectations.addLast(requestExpectation);
        responses.addLast(response);
    }

    @Override
    public StubPlan build() {
        this.compose();
        return new StubPlan(requestExpectations, responses);
    }

    /**
     * Composes the test plan. TODO Move to superclass for all siblings.
     * <p>
     * User has to implement this method when preparing a test plan. User can
     * invoke {@link #expect } inside his implementation of this method to
     * specify request expectations.
     */
    public abstract void compose();
}
