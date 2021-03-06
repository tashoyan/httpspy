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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Implementation of {@link HttpResponse} to use with {@link CamelJettyHttpSpy}.
 * <p>
 * This implementation is immutable and contains immutable fields.
 * <p>
 * TODO test.
 * <p>
 * TODO Make character encoding configurable.
 */
@Immutable
@ThreadSafe
public class CamelJettyHttpResponse implements HttpResponse {

    private final int statusCode;

    private final String body;

    private final Map<String, List<String>> headers;

    private final long delayMillis;

    /**
     * Create new instance of response.
     * 
     * @param statusCode Response status code.
     * @param body Response body.
     * @param headers Response headers. If null, then headers will be set to
     * empty map.
     * @param delayMillis Delay in milliseconds before sending the response.
     * @throws NullPointerException header name is null, list of header values
     * is null, a header value is null.
     * @throws IllegalArgumentException header name is empty or blank, list of
     * header values is empty.
     * @throws IllegalArgumentException delayMillis is negative.
     */
    protected CamelJettyHttpResponse(int statusCode, String body,
            Map<String, List<String>> headers, long delayMillis) {
        Validate.isTrue(delayMillis >= 0, "delayMillis must be >= 0");
        this.statusCode = statusCode;
        this.body = body;
        this.delayMillis = delayMillis;
        if (MapUtils.isEmpty(headers)) {
            this.headers = Collections.emptyMap();
        } else {
            headers.forEach((headerName, headerValues) -> {
                Validate.notBlank(headerName, "headerName must not be blank");
                Validate.notEmpty(headerValues,
                        "headerValues must not be null or empty, header: "
                                + headerName);
                headerValues.forEach(headerValue -> Validate.notNull(headerValue,
                        "headerValue must not be null, header: "
                                + headerName));
            });
            this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        }
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public long getDelayMillis() {
        return delayMillis;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
