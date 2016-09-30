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
import org.hamcrest.Matcher;

/**
 * Request expectation.
 * <p>
 * Provides expectations for a request received by {@link HttpSpy}. During
 * verification, the spy server compares every actual request received with its
 * expectation. In addition, expectation may declare how the spy server should
 * response on the request.
 * <p>
 * <b>Concurrency notes.</b> User creates an object of this class when setting
 * expectations in the thread that runs the test - typically {@code main}
 * thread. During the test execution, another thread reads data from the
 * expectation object, but does not modify the object. Finally, the original
 * thread reads the expectation object during verification. Therefore an
 * implementation should be thread safe.
 * 
 * @see HttpSpy#verify()
 */
@ThreadSafe
public interface RequestExpectation {

    /**
     * Gets matcher to check an actual request against this expectation.
     * 
     * @return Matcher for actual requests. Should never return null.
     */
    Matcher<HttpRequest> getRequestMatcher();
}
