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

import java.util.List;
import java.util.Map;
import net.jcip.annotations.ThreadSafe;

/**
 * HTTP response.
 * <p>
 * This bean contains properties of HTTP response. An implementation of
 * {@link HttpSpy} uses it when sending a response on an incoming request.
 * <p>
 * <b>Concurrency notes.</b> User creates an instance of this class when setting
 * expectations before the actual test has started. During the test, another
 * thread reads data from the expectation object, and the testing thread is not
 * expected to modify the object. The class should be thread safe.
 */
@ThreadSafe
public interface HttpResponse {

    /**
     * Gets status code.
     * 
     * @return Status code of the response.
     */
    int getStatusCode();

    /**
     * Gets body.
     * 
     * @return Body of this response.
     */
    String getBody();

    /**
     * Gets headers.
     * 
     * @return Headers of this response, a header with a name may have one or
     * many values. Never returns null, returns empty map if no headers. Header
     * names are non-blank strings, header values are non-null strings.
     */
    Map<String, List<String>> getHeaders();

    /**
     * Gets delay in milliseconds before sending the response.
     * 
     * @return Delay in milliseconds. Zero means response immediately without
     * delay.
     */
    long getDelayMillis();
}
