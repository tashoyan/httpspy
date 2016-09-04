package org.httpspy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;
import org.apache.camel.Exchange;
import org.apache.camel.http.common.HttpMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

/**
 * Implementation of {@link HttpRequest} to use with {@link CamelJettyHttpSpy}.
 * <p>
 * This implementation is immutable and contains immutable fields.
 * <p>
 * TODO test.
 */
@Immutable
@ThreadSafe
public class CamelJettyHttpRequest implements HttpRequest {

    private static final int USUAL_HEADERS_NUMBER = 10;

    private static final int USUAL_HEADER_VALUES = 1;

    private final String method;

    private final String path;

    private final String body;

    private final Map<String, List<String>> headers;

    /**
     * Create new request from Exchange.
     * 
     * @param exchange Exchange object.
     * @throws NullPointerException Exchange is null.
     * @throws IllegalArgumentException Exchange does not contain HTTP In
     * message.
     * @throws IOException Cannot read HTTP request body.
     */
    public CamelJettyHttpRequest(Exchange exchange) throws IOException {
        Validate.notNull(exchange, "Exchange must not be null");
        HttpMessage httpMessage = exchange.getIn(HttpMessage.class);
        if (httpMessage == null) {
            throw new IllegalArgumentException(
                    "Exchange does not have HTTP In message");
        }
        // this.method = httpMessage.getHeader(Exchange.HTTP_METHOD,
        // String.class);
        // this.path = httpMessage.getHeader(Exchange.HTTP_METHOD,
        // String.class);
        this.body = httpMessage.getBody(String.class);
        HttpServletRequest request = httpMessage.getRequest();
        this.method = request.getMethod();
        this.path = request.getPathInfo();
        // this.body =
        // IOUtils.toString(request.getInputStream(), Charset.defaultCharset());
        this.headers = Collections.unmodifiableMap(extractHeaders(request));
    }

    private Map<String, List<String>> extractHeaders(HttpServletRequest request) {
        Map<String, List<String>> requestHeaders = new HashMap<>(USUAL_HEADERS_NUMBER);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                requestHeaders.computeIfAbsent(headerName,
                        key -> new ArrayList<>(USUAL_HEADER_VALUES)).add(headerValue);
            }
        }
        return requestHeaders;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPath() {
        return path;
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
    public List<String> getHeaderValues(String name) {
        List<String> values = headers.get(name);
        return CollectionUtils.isEmpty(values)
                ? null
                : Collections.unmodifiableList(values);
    }
}
