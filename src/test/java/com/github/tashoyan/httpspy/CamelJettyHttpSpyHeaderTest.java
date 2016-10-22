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
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CamelJettyHttpSpyHeaderTest {

    private static final String SPY_SERVER_HOST = "0.0.0.0";

    private static final int SPY_SERVER_PORT = 47604;

    private static final String SPY_SERVER_PATH = "/spyseverpath/";

    private static final String SPY_SERVER_URL = "http://"
            + SPY_SERVER_HOST + ":" + SPY_SERVER_PORT + SPY_SERVER_PATH;

    private CamelJettyHttpSpy httpSpy;

    @Before
    public void before() throws Exception {
        httpSpy =
                new CamelJettyHttpSpy(SPY_SERVER_HOST, SPY_SERVER_PORT,
                        SPY_SERVER_PATH);
        httpSpy.start();
    }

    @After
    public void after() throws Exception {
        httpSpy.stop();
    }

    @Test
    public void unexpectedHeaderValue() throws Exception {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withHeader("h1", equalTo("v1")).andResponse(
                        response().withStatus(200)));
            }
        });
        Response response =
                with().body("<body>Hello</body>")
                        .headers(Collections.singletonMap("h1", "unexpected_value"))
                        .post(SPY_SERVER_URL);
        response.then().statusCode(200);
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat("Error message reports about unexpected header value",
                    e.getMessage(),
                    both(containsString("h1")).and(containsString("unexpected_value")));
        }
    }

    @Test
    public void withHeaderAnyValue() {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withHeader("h1"));
                expect(request().withHeader("h2"));
            }
        });
        Response response =
                with().body("<body>Hello</body>")
                        .headers(Collections.singletonMap("h1", "v1"))
                        .post(SPY_SERVER_URL);
        response.then().statusCode(200);
        response = with().body("<body>Hello 2</body>").post(SPY_SERVER_URL);
        response.then().statusCode(200);
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports about non-existing header and does not report about existing header",
                    e.getMessage(),
                    allOf(not(containsString("h1")), containsString("no such header"),
                            containsString("h2")));
        }
    }

    @Test
    public void withoutHeader() throws Exception {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withoutHeader("h1").andResponse(
                        response().withStatus(200)));
            }
        });
        Response response =
                with().body("<body>Hello</body>")
                        .headers(Collections.singletonMap("h1", "without_header"))
                        .post(SPY_SERVER_URL);
        response.then().statusCode(200);
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat("Error message reports that request should be without header",
                    e.getMessage(),
                    both(containsString("h1")).and(containsString("without_header")));
        }
    }

    @Test
    public void strictHeadersUnexpectedHeader() throws Exception {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withStrictHeaders().withHeader("h1", equalTo("v1"))
                        .andResponse(response().withStatus(200)));
            }
        });
        Map<String, Object> headers = new HashMap<>(2);
        headers.put("h1", "v1");
        headers.put("unexpected_header", "v2");
        Response response =
                with().body("<body>Hello</body>").headers(headers)
                        .post(SPY_SERVER_URL);
        response.then().statusCode(200);
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports about unexpected header in the strict headers set",
                    e.getMessage(),
                    both(containsString("strict headers")).and(
                            containsString("unexpected_header")));
        }
    }

    @Test
    public void responseHeaderMultipleValues() {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().andResponse(
                        response().withStatus(200).withHeader("h1", "v1")
                                .withHeader("h1", "v2").withHeader("h1", "v3")));
            }
        });
        Response response = with().body("<body>Hello</body>").post(SPY_SERVER_URL);
        response.then().statusCode(200).header("h1", "v1,v2,v3");
        httpSpy.verify();
    }
}
