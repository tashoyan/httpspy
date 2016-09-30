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

import net.jcip.annotations.NotThreadSafe;

/**
 * HTTP Spy - a spy for an HTTP server.
 * <p>
 * HTTP Spy runs on localhost and network port specified by {@link #getPort}. It
 * provides HTTP service on the path {@link #getPath}. In addition, user can
 * configure parameters like number of servicing threads, etc.
 * <p>
 * Typical usage is:
 * 
 * <pre>
 * <code>
 *  HttpSpy httpSpy = ...
 *  // Start the spy server:
 *  httpSpy.start();
 *  // Setup expectations:
 *  httpSpy
 *   .expectRequests((new AbstractRequestExpectationListBuilder() {
 *  
 *       public void build() {
 *           expect(request()
 *                       .withBody(matching(CoreMatchers.containsString("Hello")))
 *                       .withHeader("h1", 0, matching(CoreMatchers.equalTo("v1")))
 *                       .withMethod(matching(CoreMatchers.equalTo("POST")))
 *                       .withPath(matching(CoreMatchers.equalTo("/path/")))
 *                       .andResponse(response()
 *                           .withStatus(200)
 *                           .withBody("OK")
 *                           .withHeader("h2", "v2")));
 *           expect(request()
 *                       .withBody(equalToXml(myXmlSample))
 *                       .andResponse(response()
 *                           .withStatus(500)
 *                           .withBody("Cannot help")
 *                           .withDelay(TimeUnit.MILLISECONDS, 1000)));
 *       }
 *  });
 *  ...
 *  // Execute requests...
 *  ...
 *  // Verify actual requests again expectations:
 *  httpSpy.verify();
 *  // Reset the spy server:
 *  httpSpy.reset();
 *  
 *  // Setup new expectations:
 *  httpSpy
 *   .expectRequests((new AbstractRequestExpectationListBuilder() {
 *  
 *       public void build() {
 *           expect(10, request()
 *                       .andResponse(response()));
 *       }
 *  });
 *  ...
 *  // Execute more requests...
 *  ...
 *  // Verify actual requests again expectations:
 *  httpSpy.verify();
 *  // Finally, stop the spy server:
 *  httpSpy.stop();
 *  </code>
 * </pre>
 * <p>
 * Multiple request expectations can be chained. After expectations are set, one
 * can execute actual requests. HTTP Spy records actual requests for further
 * verification and replies with responses specified via
 * {@link RequestExpectationListBuilder#response() }. If a request expectation
 * does not specify a response, then HTTP Spy will response with HTTP 400 status
 * code and empty response body.
 * <p>
 * TODO Add support for responses based on actual request content. Currently
 * response is based on the sequential number of actual request. When multiple
 * threads concurrently service requests, only one response for all requests
 * makes sense, as it is impossible to set expectations on the order of
 * concurrently arriving requests.
 * <p>
 * During verification HTTP Spy checks that all request expectations are met.
 * After verification, HTTP Spy can be stopped and all expectations reset. User
 * can continue use this spy server with new request expectations. <b>Note:</b>
 * Client application should not use cached HTTP connections after the spy
 * server restart. A request with an outdated cached connection will fail.
 * <p>
 * <b>Concurrency notes.</b> All HTTP Spy methods are expected to be called in
 * the only thread - typically {@code main} thread that executes a test.
 * However, an implementation has to provide concurrent access to some data
 * inside, for example:
 * <ul>
 * <li>the list of request expectations;
 * <li>the list of recorded actual requests.
 * </ul>
 */
@NotThreadSafe
public interface HttpSpy {

    /**
     * Gets host name.
     * 
     * @return Network host name where server runs.
     */
    String getHostname();

    /**
     * Gets network port.
     * 
     * @return Network port where server runs. Should be positive.
     */
    int getPort();

    /**
     * Gets HTTP path.
     * 
     * @return HTTP path where server is available.
     */
    String getPath();

    /**
     * Sets threads number to concurrently service incoming requests.
     * <p>
     * This method guarantees that at least the specified number of threads will
     * be available to service client requests. Depending on server
     * implementation, the real number of servicing threads may be greater.
     * 
     * @param serviceThreadsNumber Number of threads that service requests.
     * @throws IllegalArgumentException threadsNumber is not positive.
     * @throws IllegalStateException The spy server has already started.
     */
    void setServiceThreadsNumber(int serviceThreadsNumber);

    /**
     * Gets number of threads that concurrently service incoming requests.
     * 
     * @return Number of threads.
     */
    int getServiceThreadsNumber();

    /**
     * Sets request expectation.
     * 
     * @param builder Builder for the list of request expectations.
     * @return This object.
     * @throws IllegalArgumentException builder is null, or builder has lists of
     * different sizes for request expectations and responses.
     */
    HttpSpy expectRequests(RequestExpectationListBuilder builder);

    /**
     * Start the spy server.
     * <p>
     * After start the spy server services client requests.
     * 
     * @throws IllegalStateException Already started.
     */
    void start();

    /**
     * Verify that expectations are met.
     */
    void verify();

    /**
     * Reset the spy server.
     * <p>
     * The spy server resets all request expectations along with actual requests
     * received and responses.
     */
    void reset();

    /**
     * Stop the spy server.
     * <p>
     * The spy server stops servicing client requests and frees network
     * resources. The spy server also resets itself.
     * 
     * @see #reset()
     */
    void stop();
}
