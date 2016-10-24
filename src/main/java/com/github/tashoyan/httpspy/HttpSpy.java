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
 * HTTP Spy - a spy for an HTTP server.
 * <p>
 * This is an interface that defines behavior for implementations.
 * <p>
 * <b>Concurrency notes.</b> All HTTP Spy methods are expected to be called in
 * the only thread - typically {@code main} thread that executes a test.
 * However, an implementation has to provide concurrent access to some data
 * inside, for example the test plan: {@link #testPlan}.
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
     * @throws IllegalArgumentException serviceThreadsNumber is not positive.
     * @throws IllegalArgumentException serviceThreadsNumber is greater than
     * one, but current test plan does not support multiple service threads.
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
     * Specify test plan.
     * 
     * @param testPlanBuilder Builder that provides the test plan.
     * @return This object
     * @throws NullPointerException testPlanBuilder is null.
     * @throws IllegalStateException Test plan is already set.
     * @throws IllegalArgumentException New test plan does not support multiple
     * service threads, but service threads number is already set greater than
     * one.
     */
    HttpSpy testPlan(TestPlanBuilder testPlanBuilder);

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
     * <p>
     * This method delegates verification to the test plan.
     * 
     * @throws IllegalStateException Test plan is not set
     * @see #testPlan
     */
    void verify();

    /**
     * Reset the spy server.
     * <p>
     * The spy server resets its current test plan.
     * 
     * @see #testPlan
     */
    void reset();

    /**
     * Stop the spy server.
     * <p>
     * The spy server stops servicing client requests and frees network
     * resources.
     */
    void stop();
}
