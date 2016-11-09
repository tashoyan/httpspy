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

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.jcip.annotations.NotThreadSafe;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jetty9.JettyHttpComponent9;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link HttpSpy} based on camel-jetty component.
 * <p>
 * TODO Configurable chunked attribute for jetty endpoint (now hardcoded).
 */
@NotThreadSafe
public class CamelJettyHttpSpy implements HttpSpy {

    /**
     * Default host name for the spy server.
     */
    protected static final String DEFAULT_HOSTNAME = "localhost";

    /**
     * Default service threads number.
     * 
     * @see #getServiceThreadsNumber()
     */
    protected static final int DEFAULT_SERVICE_THREADS_NUMBER = 1;

    private static final int JETTY_INTERNAL_THREADS_NUMBER = 8;

    private static final String PATH_SEPARATOR = "/";

    private static final String DEFAULT_PATH = PATH_SEPARATOR;

    private static final String SPY_ROUTE_NAME = "spy-server-consumer";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CamelJettyHttpSpy.class);

    private final CamelContext camelContext;

    private final String hostname;

    private final int port;

    private final String path;

    private int serviceThreadsNumber = DEFAULT_SERVICE_THREADS_NUMBER;

    private boolean isStarted;

    private final AtomicReference<TestPlan> testPlan = new AtomicReference<>();

    /**
     * Creates new instance of spy server running on default host
     * {@link #DEFAULT_HOSTNAME}.
     * 
     * @param port Network port where server will run.
     * @param path HTTP path where server will be available. If null or empty,
     * then default path {@code /} is used. Otherwise normalized path value is
     * used with leading and trailing slash characters:
     * {@code /path/to/service/}.
     * @throws IllegalArgumentException port is negative.
     * @throws IllegalArgumentException path contains illegal characters.
     */
    public CamelJettyHttpSpy(int port, String path) {
        this(DEFAULT_HOSTNAME, port, path);
    }

    /**
     * Creates new instance of spy server.
     * <p>
     * TODO; Do we need this constructor as far as we run it always on
     * localhost?
     * 
     * @param hostname Host name where server will run.
     * @param port Network port where server will run.
     * @param path HTTP path where server will be available. If null or empty,
     * then default path {@code /} is used. Otherwise normalized path value is
     * used with leading and trailing slash characters:
     * {@code /path/to/service/}.
     * @throws NullPointerException hostname is null.
     * @throws IllegalArgumentException hostname is empty or blank.
     * @throws IllegalArgumentException port is negative.
     * @throws IllegalArgumentException path contains illegal characters.
     */
    public CamelJettyHttpSpy(String hostname, int port, String path) {
        Validate.notBlank(hostname, "hostname must not be blank");
        Validate.isTrue(port > 0, "port must be > 0");
        this.camelContext = new DefaultCamelContext();
        this.hostname = hostname;
        this.port = port;
        this.path = normalizedPath(path);
    }

    private String normalizedPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return DEFAULT_PATH;
        }
        if (StringUtils.contains(path, ' ')) {
            throw new IllegalArgumentException("HTTP path must not contain spaces: "
                    + path);
        }
        String normalizedPath = path;
        if (!path.startsWith(PATH_SEPARATOR)) {
            normalizedPath = PATH_SEPARATOR
                    + normalizedPath;
        }
        if (!path.endsWith(PATH_SEPARATOR)) {
            normalizedPath = normalizedPath
                    + PATH_SEPARATOR;
        }
        return normalizedPath;
    }

    /**
     * Gets real number of threads that is necessary to be configured for Jetty
     * server.
     * <p>
     * Calculate the number of threads to be configured in Jetty server in order
     * to guarantee that Jetty server provides at least
     * {@link #getServiceThreadsNumber() } threads servicing client requests.
     * This method leverages the secret knowledge about Jetty: no more than 4
     * threads for acceptors, no more than 4 threads for selectors.
     * 
     * @return Number of threads to specify for Jetty server configuration.
     */
    protected int getRealJettyThreadsNumber() {
        return getServiceThreadsNumber()
                + JETTY_INTERNAL_THREADS_NUMBER;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setServiceThreadsNumber(int serviceThreadsNumber) {
        Validate.isTrue(serviceThreadsNumber > 0, "threadsNumber must be > 0");
        if (isStarted) {
            throw new IllegalStateException("Spy server has already started");
        }
        TestPlan plan = testPlan.get();
        if (plan != null
                && !plan.isMultithreaded() && serviceThreadsNumber > 1) {
            throw new IllegalArgumentException("Current test plan "
                    + plan + " does not support multiple service threads: "
                    + serviceThreadsNumber);
        }
        this.serviceThreadsNumber = serviceThreadsNumber;
    }

    @Override
    public int getServiceThreadsNumber() {
        return serviceThreadsNumber;
    }

    @Override
    public HttpSpy testPlan(TestPlanBuilder testPlanBuilder) {
        Validate.notNull(testPlanBuilder, "testPlanBuilder must not be null");
        TestPlan plan = testPlanBuilder.build();
        if (!plan.isMultithreaded()
                && serviceThreadsNumber > 1) {
            throw new IllegalArgumentException("New test plan "
                    + plan + " does not support multiple service threads: "
                    + serviceThreadsNumber);
        }
        if (!testPlan.compareAndSet(null, plan)) {
            throw new IllegalStateException("Test plan is already set");
        }
        return this;
    }

    @Override
    public void start() {
        if (isStarted) {
            throw new IllegalStateException("Spy server has already started");
        }
        isStarted = true;
        try {
            LOGGER.debug(
                    "Starting: {} servicing ({} real) threads, host {}, port {}, path {}",
                    getServiceThreadsNumber(), getRealJettyThreadsNumber(),
                    getHostname(), getPort(), getPath());
            camelContext.start();
            JettyHttpComponent9 jettyComponent =
                    camelContext.getComponent("jetty", JettyHttpComponent9.class);
            jettyComponent.setMinThreads(getRealJettyThreadsNumber());
            jettyComponent.setMaxThreads(getRealJettyThreadsNumber());
            camelContext.addRoutes(new RouteBuilder(camelContext) {

                @Override
                public void configure() {
                    from(
                            "jetty:http://"
                                    + getHostname() + ":" + getPort() + getPath()
                                    + "?sendServerVersion=false" + "&chunked=false"
                                    + "&disableStreamCache=true").process(
                            createSpyProcessor()).setId(SPY_ROUTE_NAME);
                }
            });
            LOGGER.info("HTTP Spy is running: {} servicing threads on {}:{}{}",
                    getServiceThreadsNumber(), getHostname(), getPort(), getPath());
        } catch (Exception e) {
            throw new RuntimeException("Exception while setting up Camel context", e);
        }
    }

    /**
     * Creates Camel Processor that will be used to record actual requests and
     * send responses back.
     * 
     * @return Processor instance.
     * @throws IllegalStateException Test plan is not set.
     */
    protected Processor createSpyProcessor() {
        Processor processor = exchange -> {
            if (testPlan.get() == null) {
                throw new IllegalStateException("Test plan is not set");
            }
            HttpRequest actualRequest = new CamelJettyHttpRequest(exchange);
            LOGGER.debug("Received actual request: {}", actualRequest);
            HttpResponse response = testPlan.get().getResponse(actualRequest);
            try {
                LOGGER.debug("Sending response: {}", response);
                sendResponseInExchange(response, exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            }
        };
        return processor;
    }

    /**
     * Send the response in Camel exchange.
     * <p>
     * This implementation sends response body in platform default encoding.
     * <p>
     * If the response has multiple headers with the same name, then this method
     * follows RFC 2616, Section 4.2 Message Headers: combine all values into
     * one string of comma-separated values.
     * 
     * @param response The response to send.
     * @param exchange Send the response as Out message within this exchange
     * object. Out message allows to drop all headers came with In message.
     * @throws NullPointerException response is null, exchange is null.
     * @throws InterruptedException Interrupted while waiting the delay,
     * specified for the response.
     * @see HttpResponse#getDelayMillis()
     */
    protected void sendResponseInExchange(HttpResponse response, Exchange exchange)
            throws InterruptedException {
        Validate.notNull(response, "response must not be null");
        Validate.notNull(exchange, "exchange must not be null");
        Message message = exchange.getOut();
        message.setHeader(Exchange.HTTP_RESPONSE_CODE, response.getStatusCode());
        message.setHeader(Exchange.HTTP_CHARACTER_ENCODING, Charset.defaultCharset()
                .name());
        message.setBody(response.getBody(), String.class);
        response.getHeaders()
                .entrySet()
                .forEach(
                        entry -> message.setHeader(entry.getKey(), entry.getValue()
                                .stream().collect(Collectors.joining(","))));
        Thread.sleep(response.getDelayMillis());
    }

    @Override
    public void verify() {
        if (testPlan.get() == null) {
            throw new IllegalStateException("Test plan is not set");
        }
        testPlan.get().verify();
    }

    @Override
    public void reset() {
        testPlan.set(null);
    }

    @Override
    public void stop() {
        isStarted = false;
        try {
            LOGGER.debug("Stopping HTTP Spy on host {}, port {}, path {}",
                    getHostname(), getPort(), getPath());
            camelContext.stop();
            camelContext.removeRoute(SPY_ROUTE_NAME);
            LOGGER.info("HTTP Spy is stopped");
        } catch (Exception e) {
            throw new RuntimeException("Exception while shutting down Camel context",
                    e);
        }
    }
}
