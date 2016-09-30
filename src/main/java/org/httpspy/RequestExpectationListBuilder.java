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
import net.jcip.annotations.NotThreadSafe;

/**
 * Builder that constructs a list of request expectations.
 * <p>
 * HTTP Spy server uses the builder to construct {@link RequestExpectation
 * request expectations}. User supplies an instance of the builder to method
 * {@link HttpSpy#expectRequests}. One instance can be used to construct
 * multiple request expectations.
 * <p>
 * <b>Concurrency notes.</b> Builder instances are used in a single thread that
 * prepares requests expectations - typically {@code main} thread.
 * Implementations are not required to be thread safe.
 *
 * @see HttpSpy#expectRequests
 */
@NotThreadSafe
public interface RequestExpectationListBuilder {

    /**
     * Gets the list of request expectations.
     * 
     * @return The list of request expectations. Never returns null, if unset
     * returns empty list.
     */
    List<RequestExpectation> getRequestExpectations();

    /**
     * Gets the list of responses on actual requests.
     * 
     * @return The list of responses. Never returns null, if unset returns empty
     * list.
     */
    List<HttpResponse> getResponses();

    /**
     * Builds the list of request expectations.
     * <p>
     * User has to implement this method in order to provide his expectations.
     */
    void build();

    /**
     * Adds a request expectation to this list.
     * 
     * @param requestExpectationBuilder Request expectation builder.
     * @throws NullPointerException requestExpectationBuilder is null.
     */
    void expect(RequestExpectationBuilder requestExpectationBuilder);

    /**
     * Returns new request expectation builder that user will use to specify his
     * expectations.
     * 
     * @return New request expectation builder. Never returns null, new object
     * provides default expectations.
     */
    RequestExpectationBuilder request();

    /**
     * Returns new response builder that user will use to describe a response on
     * the actual request.
     * 
     * @return New response builder. Never returns null, new object provides
     * default response values.
     */
    ResponseBuilder response();
}
