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

/* TODO Simplify all test names. */
public class CamelJettyHttpSpyStubTest extends CamelJettyHttpSpyTestHarness {

    @Test
    public void methodMatch() {
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
    public void methodUnmatch() {
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
    public void pathMatch() {
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
    public void bodyMatch() {
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
    public void bodyUnmatch() {
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
    public void headersMatch() {
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
    public void headersUnmatch() {
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
}
