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

import java.util.concurrent.TimeUnit;
import net.jcip.annotations.NotThreadSafe;

/**
 * Response builder.
 * <p>
 * Builds a {@link HttpResponse response object } after properties are set.
 * <p>
 * <b>Concurrency notes.</b> Builder instances are used in a single thread that
 * prepares requests expectations - typically {@code main} thread.
 * Implementations are not required to be thread safe.
 */
@NotThreadSafe
public interface ResponseBuilder {

    /**
     * Builds response.
     * 
     * @return Response object. Should not return null, if none parameters set
     * then should return default response: success, empty body, no headers,
     * without delay.
     */
    HttpResponse build();

    /**
     * Specifies response status code.
     * 
     * @param statusCode Response status code.
     * @return This object.
     * @throws IllegalArgumentException statusCode is not positive.
     */
    ResponseBuilder withStatus(int statusCode);

    /**
     * Specifies response body.
     * 
     * @param body Response body.
     * @return This object.
     */
    ResponseBuilder withBody(String body);

    /**
     * Specifies response header.
     * 
     * @param headerName Header name.
     * @param headerValue Header value.
     * @return This object.
     * @throws NullPointerException headerName is null.
     * @throws IllegalArgumentException headerName is empty or blank.
     * @throws NullPointerException headerValue is null.
     */
    ResponseBuilder withHeader(String headerName, String headerValue);

    /**
     * Specifies response delay.
     * 
     * @param timeUnit Time unit for delay.
     * @param delay Delay before sending response.
     * @return This object.
     * @throws NullPointerException timeUnit is null.
     * @throws IllegalArgumentException delay is negative.
     */
    ResponseBuilder withDelay(TimeUnit timeUnit, long delay);
}
