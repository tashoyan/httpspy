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

import static com.jayway.restassured.RestAssured.with;
import com.jayway.restassured.response.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class CamelJettyHttpSpySimpleTest extends CamelJettyHttpSpyTestHarness {

    @Test
    public void oneRequestWithResponse() {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request()
                        .withBody(equalTo("<body>Hello</body>"))
                        .withHeader("h1", 0, equalTo("v1"))
                        .withHeader("h2", 0, equalToIgnoreCase("v2"))
                        .withMethod(equalTo("POST"))
                        .withPath(equalTo(SPY_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK")
                                        .withHeader("h2", "v2")));
            }
        });
        Map<String, Object> headers = new HashMap<>(2);
        headers.put("h1", "v1");
        headers.put("h2", "V2");
        Response response =
                with().body("<body>Hello</body>").headers(headers)
                        .post(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("OK")).header("h2", "v2");
        httpSpy.verify();
    }

    @Test
    public void oneRequestWithoutResponse() {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withBody(equalTo("<body>Hello</body>"))
                        .withHeader("h1", equalTo("v1")).withMethod(equalTo("POST"))
                        .withPath(equalTo(SPY_SERVER_PATH)));
            }
        });
        Response response =
                with().body("<body>Hello</body>")
                        .headers(Collections.singletonMap("h1", "v1"))
                        .post(SPY_SERVER_URL);
        response.then().statusCode(200).body(is(""));
        httpSpy.verify();
    }

    @Test
    public void emptyRequestExpectation() {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request());
            }
        });
        Response response =
                with().body("<body>Hello</body>")
                        .headers(Collections.singletonMap("h1", "v1"))
                        .post(SPY_SERVER_URL);
        response.then().statusCode(200).body(is(""));
        httpSpy.verify();
    }

    @Test
    public void manyRequestsAllWithResponses() {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("1"))))
                        .withHeader("request", matching(endsWith("1")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(SPY_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK1")
                                        .withHeader("reply", "v1")));
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("2"))))
                        .withHeader("request", matching(endsWith("2")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(SPY_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK2")
                                        .withHeader("reply", "v2")));
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("3"))))
                        .withHeader("request", matching(endsWith("3")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(SPY_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK3")
                                        .withHeader("reply", "v3")));
            }
        });
        int requestsNumber = 3;
        for (int i = 1; i <= requestsNumber; i++) {
            Response response = with().body("body"
                    + i).headers(Collections.singletonMap("request", "v"
                    + i)).post(SPY_SERVER_URL);
            response.then().statusCode(200).body(is("OK"
                    + i)).header("reply", "v"
                    + i);
        }
        httpSpy.verify();
    }

    @Test
    public void manyRequestsSomeWithResponses() {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("1"))))
                        .withHeader("request", matching(endsWith("1")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(SPY_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK1")
                                        .withHeader("reply", "v1")));
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("2"))))
                        .withHeader("request", matching(endsWith("2")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(SPY_SERVER_PATH)));
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("3"))))
                        .withHeader("request", matching(endsWith("3")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(SPY_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK3")
                                        .withHeader("reply", "v3")));
            }
        });
        int requestsNumber = 3;
        for (int i = 1; i <= requestsNumber; i++) {
            Response response = with().body("body"
                    + i).headers(Collections.singletonMap("request", "v"
                    + i)).post(SPY_SERVER_URL);
            response.then().statusCode(200);
            if (i == 2) {
                response.then().body(is("")).header("reply", nullValue());
            } else {
                response.then().body(is("OK"
                        + i)).header("reply", "v"
                        + i);
            }
        }
        httpSpy.verify();
    }

    @Test
    public void sameRequestWithResponseManyTimes() {
        int requestsNumber = 10;
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(requestsNumber,
                        request()
                                .withBody(equalTo("<body>Hello</body>"))
                                .withHeader("request")
                                .withMethod(equalTo("POST"))
                                .withPath(equalTo(SPY_SERVER_PATH))
                                .andResponse(
                                        response().withStatus(200).withBody("OK")
                                                .withHeader("reply", "v")));
            }
        });
        for (int i = 0; i < requestsNumber; i++) {
            Response response =
                    with().body("<body>Hello</body>")
                            .headers(Collections.singletonMap("request", "v"))
                            .post(SPY_SERVER_URL);
            response.then().statusCode(200).body(is("OK")).header("reply", "v");
        }
        httpSpy.verify();
    }

    @Test
    public void manyRequestsWithResponsesInLoop() {
        int expectRequestsNumber = 5;
        for (int i = 0; i < expectRequestsNumber; i++) {
            String expectedBody = "request-"
                    + i;
            String responseBody = "response-"
                    + i;
            httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

                @Override
                public void build() {
                    expect(request().withBody(equalTo(expectedBody)).andResponse(
                            response().withBody(responseBody)));
                }
            });
        }
        for (int i = 0; i < expectRequestsNumber; i++) {
            Response response = with().body("request-"
                    + i).post(SPY_SERVER_URL);
            response.then().statusCode(200).body(is("response-"
                    + i));
        }
    }

    @Test
    public void moreRequestsThanExpectations() {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withBody(equalTo("one")));
                expect(request().withBody(equalTo("two")));
            }
        });
        Response response1 = with().body("one").post(SPY_SERVER_URL);
        response1.then().statusCode(200);
        Response response2 = with().body("two").post(SPY_SERVER_URL);
        response2.then().statusCode(200);
        Response response3 = with().body("three").post(SPY_SERVER_URL);
        response3
                .then()
                .statusCode(500)
                .body(allOf(containsString("No responses anymore"),
                        containsString("exptected requests: 2"),
                        containsString("actually received requests: 3"),
                        containsString("actual request:"), containsString("three")));
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports about unexpected request",
                    e.getMessage(),
                    allOf(containsString("Number of actually received requests"),
                            containsString(" 3"),
                            containsString("number of request expected"),
                            containsString(" 2")));
        }
    }

    @Test
    public void useSameSpyServerServeralTimes() {
        int reuseNumber = 5;
        for (int i = 0; i < reuseNumber; i++) {
            httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

                @Override
                public void build() {
                    expect(request());
                }
            });
            Response response = with().body("request-"
                    + i).post(SPY_SERVER_URL);
            response.then().statusCode(200);
            httpSpy.verify();
            httpSpy.reset();
        }
    }
}
