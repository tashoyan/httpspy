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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpClientConfigurer;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class StubConcurrentTest {

    private static final String TO_SPY_SERVER_ENDPOINT = "direct:to-spy-server";

    private static final int SPY_SERVER_PORT = 47604;

    private static final String SPY_SERVER_PATH = "/spyseverpath/";

    private CamelJettyHttpSpy httpSpy;

    private CamelContext camelContext;

    private ProducerTemplate producerTemplate;

    @BeforeClass
    public static void useProductionLogging() {
        URL config = StubConcurrentTest.class.getResource("log4j-production.xml");
        System.out.println("Setting production logging: "
                + config.getFile());
        LogManager.resetConfiguration();
        DOMConfigurator.configure(config);
    }

    private List<Integer> createServerThreadsNumbers() {
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processors: "
                + processors);
        List<Integer> serverThreadsNumbers = new ArrayList<>(processors);
        for (int i = 2; i <= processors; i += 2) {
            serverThreadsNumbers.add(i);
        }
        return serverThreadsNumbers;
    }

    private void startCamel(int clientRequestsNumber) throws Exception {
        camelContext = new DefaultCamelContext();
        HttpClientConfigurer httpClientConfigurer = clientBuilder -> {
            clientBuilder.setConnectionReuseStrategy((response, context) -> {
                return false;
            });
        };
        HttpComponent httpComponent =
                camelContext.getComponent("http4", HttpComponent.class);
        httpComponent.setHttpClientConfigurer(httpClientConfigurer);
        httpComponent.setMaxTotalConnections(clientRequestsNumber);
        httpComponent.setConnectionsPerRoute(clientRequestsNumber);
        camelContext.addRoutes(new RouteBuilder(camelContext) {

            @Override
            public void configure() throws Exception {
                from(TO_SPY_SERVER_ENDPOINT).to("http4://localhost:"
                        + SPY_SERVER_PORT + SPY_SERVER_PATH).setId("test-producer");
            }
        });
        camelContext.start();
        producerTemplate = new DefaultProducerTemplate(camelContext);
        producerTemplate.start();
    }

    private void stopCamel() throws Exception {
        producerTemplate.stop();
        camelContext.stop();
    }

    private long runTest(int clientRequestsNumber, int serverThreadsNumber,
            long executionTime) throws InterruptedException {
        httpSpy = new CamelJettyHttpSpy(SPY_SERVER_PORT, SPY_SERVER_PATH);
        httpSpy.setServiceThreadsNumber(serverThreadsNumber);
        httpSpy.start();
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().andResponse(
                        response().withDelay(TimeUnit.MILLISECONDS, executionTime)));
            }
        });
        Set<Thread> requestThreads = new HashSet<>(clientRequestsNumber);
        CountDownLatch allThreadsReady = new CountDownLatch(clientRequestsNumber);
        AtomicLong startTime = new AtomicLong(0);
        for (int i = 0; i < clientRequestsNumber; i++) {
            Thread requestThread = new Thread("requestthread-"
                    + i) {

                @Override
                public void run() {
                    allThreadsReady.countDown();
                    try {
                        allThreadsReady.await();
                    } catch (InterruptedException e) {
                        fail("Interrupted in "
                                + getName());
                    }
                    startTime.compareAndSet(0, System.currentTimeMillis());
                    producerTemplate.requestBody(TO_SPY_SERVER_ENDPOINT, "request-"
                            + getName(), String.class);
                }
            };
            requestThreads.add(requestThread);
        }
        requestThreads.stream().forEach(thread -> thread.start());
        for (Thread thread : requestThreads) {
            thread.join();
        }
        long totalExecutionTime = System.currentTimeMillis()
                - startTime.get();
        httpSpy.verify();
        httpSpy.stop();
        httpSpy.reset();
        return totalExecutionTime;
    }

    private void warmUp() throws Exception {
        startCamel(1);
        runTest(1, 1, 0);
        stopCamel();
    }

    @Test
    public void concurrentRequestsNumberEqualToServerThreadsNumber() throws Exception {
        warmUp();
        List<Integer> serverThreadsNumbers = createServerThreadsNumbers();
        if (serverThreadsNumbers.isEmpty()) {
            System.err
                    .println("Skipping the test; looks like we are running on a single-processor system");
            return;
        }
        long[] executionTimes = {100, 200, 500, 1000};
        for (int i = 0; i < executionTimes.length; i++) {
            long executionTime = executionTimes[i];
            for (Integer serverThreadsNumber : serverThreadsNumbers) {
                int clientRequestsNumber = serverThreadsNumber;
                startCamel(clientRequestsNumber);
                long totalExecutionTime =
                        runTest(clientRequestsNumber, serverThreadsNumber,
                                executionTime);
                stopCamel();
                System.out
                        .printf("Requests / server threads: %4d, total time: %5d (execute request %5d) ms\n",
                                clientRequestsNumber, totalExecutionTime,
                                executionTime);
                assertTrue("Total execution time "
                        + totalExecutionTime + " is close to request execution time "
                        + executionTime
                        + " and does not depend on the number of requests",
                        totalExecutionTime >= executionTime
                                && totalExecutionTime <= executionTime * 1.5);
            }
        }
    }

    @Test
    public void concurrentRequestsNumberGreaterThanServerThreadsNumber()
            throws Exception {
        warmUp();
        List<Integer> serverThreadsNumbers = createServerThreadsNumbers();
        if (serverThreadsNumbers.isEmpty()) {
            System.err
                    .println("Skipping the test; looks like we are running on a single-processor system");
            return;
        }
        int serverThreadsLack = 2;
        long[] executionTimes = {100, 200, 500, 1000};
        for (int i = 0; i < executionTimes.length; i++) {
            long executionTime = executionTimes[i];
            for (Integer serverThreadsNumber : serverThreadsNumbers) {
                int clientRequestsNumber = serverThreadsNumber
                        * serverThreadsLack;
                startCamel(clientRequestsNumber);
                long totalExecutionTime =
                        runTest(clientRequestsNumber, serverThreadsNumber,
                                executionTime);
                stopCamel();
                System.out
                        .printf("Requests: %4d, server threads: %4d, total time: %5d (execute request %5d) ms\n",
                                clientRequestsNumber, serverThreadsNumber,
                                totalExecutionTime, executionTime);
                assertTrue(
                        "Total execution time "
                                + totalExecutionTime
                                + " is close to (request execution time * lack of server threads) "
                                + executionTime * serverThreadsLack
                                + " and does not depend on the number of requests",
                        totalExecutionTime <= executionTime
                                * serverThreadsLack * 1.5);
            }
        }
    }
}
