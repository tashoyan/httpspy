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
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class StubSimpleTest extends CamelJettyHttpSpyTestHarness {

    @Test
    public void methodExpected_MethodMatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withMethod(equalToIgnoreCase("get")).andResponse(
                        response().withBody("Fine")));
            }
        });
        Response response = with().get(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("Fine"));
        httpSpy.verify();
    }

    @Test
    public void methodExpected_MethodUnmatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withMethod(equalToIgnoreCase("get")).andResponse(
                        response().withBody("Fine")));
            }
        });
        Response response = with().post(SPY_SERVER_URL);
        response.then()
                .statusCode(500)
                .body(allOf(containsString("Unmatched request"),
                        containsString("method=POST")));
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports all unexpected requests",
                    e.getMessage(),
                    allOf(containsString("Unmatched requests received"),
                            containsString("method=POST")));
        }
    }

    @Test
    public void pathExpected_PathMatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withPath(equalTo(SPY_SERVER_PATH)).andResponse(
                        response().withBody("Fine")));
            }
        });
        Response response = with().get(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("Fine"));
        httpSpy.verify();
    }

    @Test
    public void bodyExpected_BodyMatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withBody(matching(containsString("Hello")))
                        .andResponse(response().withBody("Fine")));
            }
        });
        Response response = with().body("Hello world").get(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("Fine"));
        httpSpy.verify();
    }

    @Test
    public void bodyExpected_BodyUnmatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withBody(matching(containsString("Hello")))
                        .andResponse(response().withBody("Fine")));
            }
        });
        Response response = with().body("Farewell").get(SPY_SERVER_URL);
        response.then()
                .statusCode(500)
                .body(allOf(containsString("Unmatched request"),
                        containsString("method=GET"), containsString("path="
                                + SPY_SERVER_PATH), containsString("Farewell")));
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports all unexpected requests",
                    e.getMessage(),
                    allOf(containsString("Unmatched requests received"),
                            containsString("method=GET"), containsString("path="
                                    + SPY_SERVER_PATH), containsString("Farewell")));
        }
    }

    @Test
    public void headersExpected_HeadersMatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withHeader("h1", equalTo("v1")).andResponse(
                        response().withBody("Fine")));
            }
        });
        Response response = with().header("h1", "v1").get(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("Fine"));
        httpSpy.verify();
    }

    @Test
    public void headersExpected_HeadersUnmatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withHeader("h1", equalTo("v1")).andResponse(
                        response().withBody("Fine")));
            }
        });
        Response response = with().header("h1", "v2").get(SPY_SERVER_URL);
        response.then()
                .statusCode(500)
                .body(allOf(containsString("Unmatched request"),
                        containsString("method=GET"), containsString("path="
                                + SPY_SERVER_PATH), containsString("h1"),
                        containsString("v2")));
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports all unexpected requests",
                    e.getMessage(),
                    allOf(containsString("Unmatched requests received"),
                            containsString("method=GET"), containsString("path="
                                    + SPY_SERVER_PATH), containsString("h1"),
                            containsString("v2")));
        }
    }

    @Test
    public void allFeaturesExpected_MethodMatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withMethod(equalTo("GET"))
                        .withPath(equalTo(SPY_SERVER_PATH)).withBody(equalTo("Hello"))
                        .withHeader("h1", equalTo("v1"))
                        .andResponse(response().withBody("Fine")));
            }
        });
        Response response = with().get(SPY_SERVER_URL);
        response.then()
                .statusCode(500)
                .body(allOf(containsString("Unmatched request"),
                        containsString("method=GET")));
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports all unexpected requests",
                    e.getMessage(),
                    allOf(containsString("Unmatched requests received"),
                            containsString("method=GET")));
        }
    }

    @Test
    public void allFeaturesExpected_PathMatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withMethod(equalTo("GET"))
                        .withPath(equalTo(SPY_SERVER_PATH)).withBody(equalTo("Hello"))
                        .withHeader("h1", equalTo("v1"))
                        .andResponse(response().withBody("Fine")));
            }
        });
        Response response = with().post(SPY_SERVER_URL);
        response.then()
                .statusCode(500)
                .body(allOf(containsString("Unmatched request"),
                        containsString("method=POST")));
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports all unexpected requests",
                    e.getMessage(),
                    allOf(containsString("Unmatched requests received"),
                            containsString("method=POST")));
        }
    }

    @Test
    public void allFeaturesExpected_BodyMatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withMethod(equalTo("GET"))
                        .withPath(equalTo(SPY_SERVER_PATH)).withBody(equalTo("Hello"))
                        .withHeader("h1", equalTo("v1"))
                        .andResponse(response().withBody("Fine")));
            }
        });
        Response response = with().body("Hello").post(SPY_SERVER_URL);
        response.then()
                .statusCode(500)
                .body(allOf(containsString("Unmatched request"),
                        containsString("method=POST"), containsString("Hello")));
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports all unexpected requests",
                    e.getMessage(),
                    allOf(containsString("Unmatched requests received"),
                            containsString("method=POST"), containsString("Hello")));
        }
    }

    @Test
    public void allFeaturesExpected_HeadersMatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withMethod(equalTo("GET"))
                        .withPath(equalTo(SPY_SERVER_PATH)).withBody(equalTo("Hello"))
                        .withHeader("h1", equalTo("v1"))
                        .andResponse(response().withBody("Fine")));
            }
        });
        Response response = with().header("h1", "v1").post(SPY_SERVER_URL);
        response.then()
                .statusCode(500)
                .body(allOf(containsString("Unmatched request"),
                        containsString("method=POST"), containsString("h1"),
                        containsString("v1")));
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports all unexpected requests",
                    e.getMessage(),
                    allOf(containsString("Unmatched requests received"),
                            containsString("method=POST"), containsString("h1"),
                            containsString("v1")));
        }
    }

    @Test
    public void allFeaturesExpected_AllFeaturesMatch() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withMethod(equalTo("GET"))
                        .withPath(equalTo(SPY_SERVER_PATH)).withBody(equalTo("Hello"))
                        .withHeader("h1", equalTo("v1"))
                        .andResponse(response().withBody("Fine")));
            }
        });
        Response response =
                with().body("Hello").header("h1", "v1").get(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("Fine"));
        httpSpy.verify();
    }

    @Test
    public void twoExpectations_MatchFirst() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withBody(equalTo("Hello"))
                        .withHeader("h1", equalToIgnoreCase("v1"))
                        .andResponse(response().withBody("First")));
                expect(request().withMethod(matching(containsString("GET")))
                        .withBody(equalTo("Hello"))
                        .andResponse(response().withBody("Second")));
            }
        });
        Response response =
                with().body("Hello").header("h1", "V1").post(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("First"));
    }

    @Test
    public void twoExpectations_MatchSecond() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withBody(equalTo("Hello"))
                        .withHeader("h1", equalToIgnoreCase("v1"))
                        .andResponse(response().withBody("First")));
                expect(request().withMethod(matching(containsString("GET")))
                        .withBody(equalTo("Hello"))
                        .andResponse(response().withBody("Second")));
            }
        });
        Response response = with().body("Hello").get(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("Second"));
    }

    @Test
    public void twoExpectations_MatchBoth() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withBody(equalTo("Hello"))
                        .withHeader("h1", equalToIgnoreCase("v1"))
                        .andResponse(response().withBody("First")));
                expect(request().withMethod(matching(containsString("GET")))
                        .withBody(equalTo("Hello"))
                        .andResponse(response().withBody("Second")));
            }
        });
        Response response =
                with().body("Hello").header("h1", "V1").get(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("Second"));
    }

    @Test
    public void threeExpectations_ThreeRequests_MatchDifferent() {
        httpSpy.testPlan(new AbstractStubPlanBuilder() {

            @Override
            public void compose() {
                expect(request().withMethod(equalTo("GET")).andResponse(
                        response().withBody("First")));
                expect(request().withBody(
                        equalToJson("{\"value1\":\"1\", \"value2\":\"2\"}"))
                        .andResponse(response().withBody("Second")));
                expect(request().withHeader("h1", matching(containsString("v1")))
                        .andResponse(response().withBody("Third")));
            }
        });
        Response response = with().header("h1", "vv11").get(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("Third"));
        response =
                with().body("{\"value2\":\"2\", \"value1\":\"1\"}")
                        .get(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("Second"));
        response = with().get(SPY_SERVER_URL);
        response.then().statusCode(200).body(is("First"));
    }
}
