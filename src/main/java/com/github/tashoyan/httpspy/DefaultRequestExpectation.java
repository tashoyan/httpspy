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

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.Validate;
import org.hamcrest.Matcher;

/**
 * Default implementation of {@link RequestExpectation}.
 */
@Immutable
@ThreadSafe
public class DefaultRequestExpectation implements RequestExpectation {

    private final Matcher<HttpRequest> requestMatcher;

    /**
     * Constructs new instance of request expectation.
     * 
     * @param requestMatcher Request matcher to check an actual request against
     * this expectation.
     * @throws NullPointerException requestMatcher is null.
     */
    protected DefaultRequestExpectation(Matcher<HttpRequest> requestMatcher) {
        Validate.notNull(requestMatcher, "requestMatcher must not be null");
        this.requestMatcher = requestMatcher;
    }

    @Override
    public Matcher<HttpRequest> getRequestMatcher() {
        return requestMatcher;
    }
}
