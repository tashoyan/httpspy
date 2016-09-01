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
     * @throws IllegalArgumentException headerName is null or empty or blank,
     * headerValue is null.
     */
    ResponseBuilder withHeader(String headerName, String headerValue);

    /**
     * Specifies response delay.
     * 
     * @param timeUnit Time unit for delay.
     * @param delay Delay before sending response.
     * @return This object.
     * @throws IllegalArgumentException timeUnit is null.
     * @throws IllegalArgumentException delay is negative.
     */
    ResponseBuilder withDelay(TimeUnit timeUnit, long delay);
}
