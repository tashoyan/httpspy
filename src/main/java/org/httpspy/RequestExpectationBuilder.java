package org.httpspy;

import net.jcip.annotations.NotThreadSafe;

/**
 * Request expectation builder.
 * <p>
 * Builds a {@link RequestExpectation request expectation object } after
 * properties are set.
 * <p>
 * <b>Concurrency notes.</b> Builder instances are used in a single thread that
 * prepares requests expectations - typically {@code main} thread.
 * Implementations are not required to be thread safe.
 */
@NotThreadSafe
public interface RequestExpectationBuilder {

    /**
     * Builds request expectation.
     * 
     * @return Request expectation. Should not return null, if none parameters
     * set then should return default request expectation that matches any
     * request.
     */
    RequestExpectation build();

    /**
     * Gets response builder to provide a response on the actual request.
     * 
     * @return Response builder. Should not return null. If not set, then should
     * return a builder that provides default response.
     */
    ResponseBuilder getResponseBuilder();

    /**
     * Specifies expected request method.
     * 
     * @param valueExpectation Expected method.
     * @return This object.
     * @throws NullPointerException valueExpectation is null.
     */
    RequestExpectationBuilder withMethod(ValueExpectation valueExpectation);

    /**
     * Specifies expected request path.
     * 
     * @param valueExpectation Expected path.
     * @return This object.
     * @throws NullPointerException valueExpectation is null.
     */
    RequestExpectationBuilder withPath(ValueExpectation valueExpectation);

    /**
     * Specifies expected request body.
     * 
     * @param valueExpectation Expected body.
     * @return This object.
     * @throws NullPointerException valueExpectation is null.
     */
    RequestExpectationBuilder withBody(ValueExpectation valueExpectation);

    /**
     * Specifies expected request header without any expectations on its value.
     * 
     * @param headerName Header name.
     * @return This object.
     * @throws NullPointerException headerName is null.
     * @throws IllegalArgumentException headerName is empty or blank.
     */
    RequestExpectationBuilder withHeader(String headerName);

    /**
     * Specifies expected request header with its value, but without position in
     * the list of header values.
     * 
     * @param headerName Header name.
     * @param valueExpectation Expected header value.
     * @return This object.
     * @throws NullPointerException valueExpectation is null.
     * @throws NullPointerException headerName is null.
     * @throws IllegalArgumentException headerName is empty or blank.
     */
    RequestExpectationBuilder withHeader(String headerName,
            ValueExpectation valueExpectation);

    /**
     * Specifies expected request header with its value and position in the list
     * of header values.
     * 
     * @param headerName Header name.
     * @param valueIndex Header value index in the list of header values.
     * @param valueExpectation Expected header value.
     * @return This object.
     * @throws NullPointerException valueExpectation is null.
     * @throws NullPointerException headerName is null.
     * @throws IllegalArgumentException headerName is empty or blank.
     * @throws IllegalArgumentException valueIndex is negative.
     */
    RequestExpectationBuilder withHeader(String headerName, int valueIndex,
            ValueExpectation valueExpectation);

    /**
     * Specifies that request is not expected to have this header.
     * 
     * @param headerName Header name.
     * @return This object.
     * @throws NullPointerException headerName is null.
     * @throws IllegalArgumentException headerName is empty or blank.
     */
    RequestExpectationBuilder withoutHeader(String headerName);

    /**
     * If the property {@code strict headers} is enabled, this expectation
     * allows only headers specified explicitly by {@link #withHeader } method.
     * <p>
     * By default this property is disabled.
     * 
     * @return This object.
     */
    RequestExpectationBuilder withStrictHeaders();

    /**
     * Specifies response builder to provide a response on the actual request.
     * 
     * @param responseBuilder Response builder.
     * @return This object.
     * @throws NullPointerException responseBuilder is null.
     */
    RequestExpectationBuilder andResponse(ResponseBuilder responseBuilder);
}
