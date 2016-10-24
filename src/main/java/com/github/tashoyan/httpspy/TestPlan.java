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

import net.jcip.annotations.ThreadSafe;

/**
 * Test plan for HTTP Spy.
 * <p>
 * A test plan provides the following functions:
 * <ul>
 * <li>Provide responses for actual requests coming to {@link HttpSpy} from the
 * system under test
 * <li>Provide verification procedure to assert that actual requests match
 * request expectations.
 * </ul>
 * <p>
 * <b>Concurrency notes.</b> Test plan instance can be accessed from multiple
 * threads serving requests inside {@link HttpSpy}. All servicing threads invoke
 * {@link #getResponse }. Finally, the main test thread invokes {@link #verify }.
 * Therefore, thread safety requirements are:
 * <ol>
 * <li>Method {@link #getResponse } provides a safe way to concurrently get
 * responses from many threads.
 * <li>Method {@link #verify } may have no special mechanism to ensure thread
 * safety. However it should be invoked only after (be means of happens-before
 * relationship) all servicing threads are done their job.
 * </ol>
 * An exception is a test plan that <b>by definition</b> can be used with single
 * servicing thread only. Such test plan is not required to be thread safe.
 */
@ThreadSafe
public interface TestPlan {

    /**
     * Gets a response for an actual request.
     * 
     * @param actualRequest A request that has been received from the system
     * under test.
     * @return Response on the request. Never returns null.
     * @throws NullPointerException actualRequest is null.
     */
    HttpResponse getResponse(HttpRequest actualRequest);

    /**
     * Verifies actual requests against request expectations.
     * 
     * @throws AssertionError Actual requests do not match request expectations.
     */
    void verify();

    /**
     * Checks if this test plan supports multiple service threads.
     * 
     * @return {@code true} if multiple service threads can work with this test
     * plan.
     */
    boolean isMultithreaded();
}
