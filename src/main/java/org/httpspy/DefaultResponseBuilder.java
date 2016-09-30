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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang3.Validate;

/**
 * Default implementation of {@link ResponseBuilder}.
 * <p>
 * TODO Test.
 */
@NotThreadSafe
public class DefaultResponseBuilder implements ResponseBuilder {

    private static final int USUAL_HEADERS_NUMBER = 10;

    private static final int USUAL_HEADER_VALUES = 1;

    private int statusCode;

    private String body;

    private Map<String, List<String>> headers;

    private long delayMillis;

    /**
     * Creates new builder instance.
     * <p>
     * New instance provides default response.
     * <p>
     * End user is not supposed to call this constructor and instead should call
     * {@link RequestExpectationBuilder#andResponse } when implementing
     * {@link AbstractRequestExpectationListBuilder#build() }.
     */
    protected DefaultResponseBuilder() {
        statusCode = HttpServletResponse.SC_OK;
        body = "";
        headers = new HashMap<>(USUAL_HEADERS_NUMBER);
        delayMillis = 0;
    }

    @Override
    public HttpResponse build() {
        return new CamelJettyHttpResponse(statusCode, body, headers, delayMillis);
    }

    @Override
    public ResponseBuilder withStatus(int statusCode) {
        Validate.isTrue(statusCode > 0, "statusCode must be positive");
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public ResponseBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public ResponseBuilder withHeader(String headerName, String headerValue) {
        Validate.notBlank(headerName, "headerName must not be blank");
        Validate.notNull(headerValue, "headerValue must not be null");
        headers.computeIfAbsent(headerName,
                key -> new ArrayList<>(USUAL_HEADER_VALUES)).add(headerValue);
        return this;
    }

    @Override
    public ResponseBuilder withDelay(TimeUnit timeUnit, long delay) {
        Validate.notNull(timeUnit, "timeUnit must not be null");
        Validate.isTrue(delay >= 0, "delay must not be negative");
        delayMillis = timeUnit.toMillis(delay);
        return this;
    }
}
