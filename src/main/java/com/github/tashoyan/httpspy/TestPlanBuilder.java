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

import net.jcip.annotations.NotThreadSafe;

/**
 * Builder for test plans.
 * <p>
 * Implementations provide ways to build plans for different kind of tests with
 * HTTP Spy.
 * <p>
 * <b>Concurrency notes.</b> Builder instances are used in a single thread that
 * prepares requests expectations - typically {@code main} thread.
 * Implementations are not required to be thread safe.
 */
@NotThreadSafe
public interface TestPlanBuilder {

    /**
     * Expect a request.
     * <p>
     * The method adds new request expectation as specified by the argument.
     * Also the method adds the response for the actual request.
     * 
     * @param requestExpectationBuilder Request expectation builder.
     * @throws NullPointerException requestExpectationBuilder is null.
     */
    void expect(RequestExpectationBuilder requestExpectationBuilder);

    /**
     * Returns new request expectation builder that user will populate when
     * specifying his expectations.
     * 
     * @return New request expectation builder. Never returns null, new object
     * provides default expectations.
     */
    RequestExpectationBuilder request();

    /**
     * Returns new response builder that user will populate when describing a
     * response on the actual request.
     * 
     * @return New response builder. Never returns null, new object provides
     * default response values.
     */
    ResponseBuilder response();

    /**
     * Builds new test plan.
     * 
     * @return New test plan. Never returns null.
     */
    TestPlan build();
}
