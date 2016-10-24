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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

/**
 * Test plan to check the sequence in which system under test executes requests.
 * <p>
 * This test plan expects the system under test to send requests in a predefined
 * sequence. If the system under test sends actual requests in a different
 * sequence, then verification step indicates a failure.
 * <p>
 * Method {@link #getResponse } returns responses in the sequence defined during
 * the test plan build. The system under test may send more actual requests,
 * than the plan expects. In this case, method {@link #getResponse } returns a
 * special response with "Internal server error" status code for all extra
 * actual requests. Method {@link #createMissingResponse } can be overridden to
 * change this behavior. Finally, verification reports a failure.
 * <p>
 * Sequence check makes sense only when all requests are serviced by a single
 * thread in HTTP Spy. Therefore, {@code SequencePlan} can be used only with the
 * number of servicing threads set to {@code 1}.
 * <p>
 * <b>Concurrency notes.</b> As far as {@code SequencePlan} can be used only
 * with one servicing thread, it is not thread safe.
 */
@NotThreadSafe
public class SequencePlan implements TestPlan {

    private final List<RequestExpectation> requestExpectations;

    private final List<HttpResponse> responses;

    private final List<HttpRequest> actualRequests;

    /**
     * Creates new instance of the test plan.
     * 
     * @param requestExpectations The list of request expectations.
     * @param responses The list of responses on actual requests.
     * @throws NullPointerException requestExpectations is null, responses is
     * null.
     * @throws IllegalArgumentException requestExpectations is empty, responses
     * is empty, requestExpectations.size != responses.size.
     */
    public SequencePlan(List<RequestExpectation> requestExpectations,
            List<HttpResponse> responses) {
        Validate.notEmpty(requestExpectations,
                "requestExpectations must not be null or empty");
        Validate.notEmpty(responses, "requestExpectations must not be null or empty");
        Validate.isTrue(requestExpectations.size() == responses.size(),
                "requestExpectations and responses must have the same size");
        this.requestExpectations =
                Collections.unmodifiableList(new ArrayList<>(requestExpectations));
        this.responses = new ArrayList<>(responses);
        this.actualRequests = new ArrayList<>(requestExpectations.size());
    }

    @Override
    public HttpResponse getResponse(HttpRequest actualRequest) {
        Validate.notNull(actualRequest, "actualRequest must not be null");
        actualRequests.add(actualRequest);
        if (!responses.isEmpty()) {
            return responses.remove(0);
        } else {
            return createMissingResponse(actualRequest);
        }
    }

    /**
     * Creates a special response when the number of actual requests exceeds the
     * number of request expectations.
     * 
     * @param actualRequest Actual request.
     * @return Response. Never returns null.
     * @throws NullPointerException actualRequest is null.
     */
    protected HttpResponse createMissingResponse(HttpRequest actualRequest) {
        Validate.notNull(actualRequest, "actualRequest must not be null");
        String msg =
                "No responses anymore; exptected requests: "
                        + requestExpectations.size()
                        + "; actually received requests: " + actualRequests.size()
                        + " actual request: " + actualRequest;
        return new CamelJettyHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg,
                Collections.emptyMap(), 0);
    }

    @Override
    public void verify() {
        int actualRequestsNumber = actualRequests.size();
        if (requestExpectations.size() != actualRequestsNumber) {
            throw new AssertionError("Number of actually received requests "
                    + actualRequestsNumber
                    + " should equal the number of request expected "
                    + requestExpectations.size());
        }
        int i = 0;
        for (RequestExpectation requestExpectation : requestExpectations) {
            Matcher<HttpRequest> requestMatcher =
                    requestExpectation.getRequestMatcher();
            HttpRequest actualRequest = actualRequests.get(i);
            MatcherAssert.assertThat("Request #"
                    + i + " should match expectation", actualRequest, requestMatcher);
            i++;
        }
    }

    @Override
    public final boolean isMultithreaded() {
        return false;
    }
}
