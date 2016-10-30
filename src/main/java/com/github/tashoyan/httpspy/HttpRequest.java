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
 * HTTP request.
 * <p>
 * This bean contains properties of HTTP request. An implementation of
 * {@link HttpSpy} uses it to record actual requests and to verify against
 * request expectations.
 * <p>
 * <b>Concurrency notes.</b> An instance of this class is written in the thread
 * that performs actual testing. Another thread reads the instance during
 * verification. The class should be thread safe.
 */
@ThreadSafe
public interface HttpRequest {

    /**
     * Gets HTTP method.
     * 
     * @return HTTP method of this request.
     */
    String getMethod();

    /**
     * Gets HTTP path.
     * 
     * @return HTTP path of this request.
     */
    String getPath();

    /**
     * Gets body.
     * 
     * @return Body of this request.
     */
    String getBody();

    /**
     * Gets headers.
     * 
     * @return Headers of this request, a header with a name may have one or
     * many values. Never returns null, returns empty map if no headers.
     */
    Map<String, List<String>> getHeaders();

    /**
     * Gets all values of specified header.
     * 
     * @param name Name of the header.
     * @return List of header values. Null if no such header in the request.
     */
    List<String> getHeaderValues(String name);
}
