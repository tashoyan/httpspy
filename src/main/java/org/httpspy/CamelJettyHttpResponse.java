package org.httpspy;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;

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
     * <p>
     * End user is not supposed to call this constructor and instead should call
     * {@link AbstractRequestExpectationListBuilder#expect }.
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
                        "headerValues must not be empty, header: "
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

    /**
     * Send this response in Camel exchange.
     * <p>
     * This implementation sends response body in platform default encoding.
     * <p>
     * If the response has multiple headers with the same name, then this method
     * follows RFC 2616, Section 4.2 Message Headers: combine all values into
     * one string of comma-separated values.
     * 
     * @param exchange Send the response as Out message within this exchange
     * object. Out message allows to drop all headers came with In message.
     * @throws NullPointerException Exchange is null.
     * @throws InterruptedException Interrupted while waiting the delay
     */
    protected void sendInExchange(Exchange exchange) throws InterruptedException {
        Validate.notNull(exchange, "Exchange must not be null");
        Message message = exchange.getOut();
        message.setHeader(Exchange.HTTP_RESPONSE_CODE, this.getStatusCode());
        message.setHeader(Exchange.HTTP_CHARACTER_ENCODING, Charset.defaultCharset()
                .name());
        message.setBody(this.getBody(), String.class);
        this.getHeaders()
                .entrySet()
                .forEach(
                        entry -> message.setHeader(entry.getKey(), entry.getValue()
                                .stream().collect(Collectors.joining(","))));
        Thread.sleep(this.getDelayMillis());
    }
}
