package com.github.tashoyan.httpspy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;

/**
 * Test plan that allows to provide responses based on features of of requests.
 * <p>
 * This test plan implements <a
 * href="http://xunitpatterns.com/Test%20Stub.html">stub test object</a> that
 * feeds the system under test with responses. The test plan chooses a response
 * based on the features of the request sent by the system under test. For
 * example, user can configure HTTP Spy to respond on all requests having body
 * {@code <html>Hello<html>} with the status code {@code 200}.
 * <p>
 * When looking for the matching response, the test plan follows the order of
 * request expectations specified in the constructor {@link #StubPlan}. In other
 * words, the first specified request expectation has the highest priority and
 * evaluated first.
 * <p>
 * If none request expectations matched for a request, then the test plan
 * replies with a special response with {@code Internal server error} status
 * code. Verification in this case will fail and failure explanation will list
 * all unmatched requests.
 */
@ThreadSafe
public class StubPlan implements TestPlan {

    private final List<Pair<RequestExpectation, HttpResponse>> expectationsAndResponses;

    private final List<HttpRequest> unmatchedRequests;

    /**
     * Creates new test plan with request expectations and responses for them.
     * <p>
     * Each request expectation has a corresponding response at the same index
     * in the list. Method {@link #getResponse } will look for a matching
     * expectation (and a corresponding response) in the order specified by the
     * arguments.
     *
     * @param requestExpectations The ordered collection of request
     * expectations.
     * @param responses The ordered collection of responses on actual requests.
     * @throws NullPointerException requestExpectations is null, responses is
     * null.
     * @throws IllegalArgumentException requestExpectations is empty, responses
     * is empty, requestExpectations.size != responses.size.
     */
    public StubPlan(Collection<RequestExpectation> requestExpectations,
            Collection<HttpResponse> responses) {
        Validate.notEmpty(requestExpectations,
                "requestExpectations must not be null or empty");
        Validate.notEmpty(responses, "requestExpectations must not be null or empty");
        Validate.isTrue(requestExpectations.size() == responses.size(),
                "requestExpectations and responses must have the same size");
        List<Pair<RequestExpectation, HttpResponse>> tmp =
                new ArrayList<>(requestExpectations.size());
        Iterator<HttpResponse> responseIt = responses.iterator();
        requestExpectations.stream().forEachOrdered((expectation) -> {
            tmp.add(new ImmutablePair<>(expectation, responseIt.next()));
        });
        expectationsAndResponses = Collections.unmodifiableList(tmp);
        unmatchedRequests =
                Collections.synchronizedList(new ArrayList<>(requestExpectations
                        .size()));
    }

    @Override
    public HttpResponse getResponse(HttpRequest actualRequest) {
        for (Pair<RequestExpectation, HttpResponse> p : expectationsAndResponses) {
            RequestExpectation expectation = p.getLeft();
            if (expectation.getRequestMatcher().matches(actualRequest)) {
                return p.getRight();
            }
        }
        recordUnmatchedRequest(actualRequest);
        return createUnmatchedRequestResponse(actualRequest);
    }

    /**
     * Records a request unmatched with all expectations.
     * <p>
     * This implementation simply stores the request for the verification.
     * 
     * @param actualRequest Actual request.
     * @throws NullPointerException actualRequest is null.
     */
    protected void recordUnmatchedRequest(HttpRequest actualRequest) {
        Validate.notNull(actualRequest, "actualRequest must not be null");
        unmatchedRequests.add(actualRequest);
    }

    /**
     * Creates a special response when a request does not match all
     * expectations.
     * <p>
     * This implementation responds with {@code Internal server error} status
     * code.
     * 
     * @param actualRequest Actual request.
     * @return Response. Never returns null.
     * @throws NullPointerException actualRequest is null.
     */
    protected HttpResponse createUnmatchedRequestResponse(HttpRequest actualRequest) {
        Validate.notNull(actualRequest, "actualRequest must not be null");
        return new CamelJettyHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "Unmatched request: "
                        + actualRequest.toString(), Collections.emptyMap(), 0);
    }

    @Override
    public void verify() {
        if (unmatchedRequests.isEmpty()) {
            return;
        }
        throw new AssertionError("Unmatched requests received:\n"
                + StringUtils.join(unmatchedRequests, '\n'));
    }

    @Override
    public boolean isMultithreaded() {
        return true;
    }
}
