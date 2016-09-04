package org.httpspy;

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
