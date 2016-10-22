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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.jcip.annotations.NotThreadSafe;
import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jetty9.JettyHttpComponent9;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

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

    static final int DEFAULT_REQUESTS_NUMBER = 1000;

    private static final int JETTY_INTERNAL_THREADS_NUMBER = 8;

    private static final String PATH_SEPARATOR = "/";

    private static final String DEFAULT_PATH = PATH_SEPARATOR;

    private static final String SPY_ROUTE_NAME = "spy-server-consumer";

    private final CamelContext camelContext;

    private final String hostname;

    private final int port;

    private final String path;

    private int serviceThreadsNumber = DEFAULT_SERVICE_THREADS_NUMBER;

    private boolean isStarted;

    private final List<RequestExpectation> requestExpectations = new ArrayList<>(
            DEFAULT_REQUESTS_NUMBER);

    private final List<HttpRequest> actualRequests = new ArrayList<>(
            DEFAULT_REQUESTS_NUMBER);

    private final List<HttpResponse> responses = new ArrayList<>(
            DEFAULT_REQUESTS_NUMBER);

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
        this.serviceThreadsNumber = serviceThreadsNumber;
    }

    @Override
    public int getServiceThreadsNumber() {
        return serviceThreadsNumber;
    }

    @Override
    public HttpSpy expectRequests(RequestExpectationListBuilder builder) {
        builder.build();
        Validate.isTrue(builder.getRequestExpectations().size() == builder
                .getResponses().size(),
                "requestExpectations and responses must have the same size");
        this.requestExpectations.addAll(builder.getRequestExpectations());
        this.responses.addAll(builder.getResponses());
        this.actualRequests.clear();
        return this;
    }

    @Override
    public void start() {
        if (isStarted) {
            throw new IllegalStateException("Spy server has already started");
        }
        isStarted = true;
        try {
            camelContext.start();
            JettyHttpComponent9 jettyComponent =
                    camelContext.getComponent("jetty", JettyHttpComponent9.class);
            jettyComponent.setMinThreads(getRealJettyThreadsNumber());
            jettyComponent.setMaxThreads(getRealJettyThreadsNumber());
            camelContext.addRoutes(new RouteBuilder(camelContext) {

                @Override
                public void configure() throws Exception {
                    from(
                            "jetty:http://"
                                    + getHostname() + ":" + getPort() + getPath()
                                    + "?sendServerVersion=false" + "&chunked=false"
                                    + "&disableStreamCache=true").process(
                            createSpyProcessor()).setId(SPY_ROUTE_NAME);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Exception while setting up Camel context", e);
        }
    }

    /**
     * Creates Camel Processor that will be used to record actual requests and
     * send responses back.
     * 
     * @return Processor instance.
     * @throws AssertionError No response available for an actual requests. This
     * happens when the number of actual requests is greater, than the number of
     * expected requests and responses.
     */
    protected Processor createSpyProcessor() {
        Processor processor =
                exchange -> {
                    HttpRequest actualRequest = new CamelJettyHttpRequest(exchange);
                    CamelJettyHttpResponse response;
                    synchronized (CamelJettyHttpSpy.this) {
                        actualRequests.add(actualRequest);
                        if (!responses.isEmpty()) {
                            response = (CamelJettyHttpResponse) responses.remove(0);
                        } else {
                            String msg =
                                    "No responses anymore; exptected requests: "
                                            + requestExpectations.size()
                                            + "; actually received requests: "
                                            + actualRequests.size()
                                            + " actual request: " + actualRequest;
                            response =
                                    new CamelJettyHttpResponse(
                                            HttpStatus.SC_INTERNAL_SERVER_ERROR, msg,
                                            Collections.emptyMap(), 0);
                        }
                    }
                    try {
                        response.sendInExchange(exchange);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                };
        return processor;
    }

    @Override
    public void verify() {
        int actualRequestsNumber;
        synchronized (this) {
            actualRequestsNumber = actualRequests.size();
        }
        if (requestExpectations.size() != actualRequestsNumber) {
            throw new AssertionError("Number of actually received requests "
                    + actualRequestsNumber
                    + " should equal the number of request expected "
                    + requestExpectations.size());
        }
        int i = 0;
        for (RequestExpectation requestExpectation : requestExpectations) {
            Matcher<HttpRequest> requestMatcher =
                    requestExpectation.getRequestMatcher();
            HttpRequest actualRequest = actualRequests.get(i);
            MatcherAssert.assertThat("Request #"
                    + i + " should match expectation", actualRequest, requestMatcher);
            i++;
        }
    }

    @Override
    public void reset() {
        synchronized (this) {
            requestExpectations.clear();
            actualRequests.clear();
            responses.clear();
        }
    }

    @Override
    public void stop() {
        isStarted = false;
        reset();
        try {
            camelContext.stop();
            camelContext.removeRoute(SPY_ROUTE_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Exception while shutting down Camel context",
                    e);
        }
    }
}
