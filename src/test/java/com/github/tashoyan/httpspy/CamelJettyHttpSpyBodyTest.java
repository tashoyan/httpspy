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
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class CamelJettyHttpSpyBodyTest {

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
    public void bodyEqualToXml() throws Exception {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withBody(equalToXml("<xml><color>red</color></xml>")));
            }
        });
        Response response =
                with().body("<xml><color>red</color></xml>").post(SPY_SERVER_URL);
        response.then().statusCode(200).body(is(""));
        httpSpy.verify();
    }

    @Test
    public void bodyNotEqualToXml() throws Exception {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withBody(equalToXml("<xml><color>red</color></xml>")));
            }
        });
        Response response =
                with().body("<xml><color>green</color></xml>").post(SPY_SERVER_URL);
        response.then().statusCode(200).body(is(""));
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports about XML value mismatch",
                    e.getMessage(),
                    both(containsString("<xml><color>red</color></xml>")).and(
                            containsString("<xml><color>green</color></xml>")));
        }
    }

    @Test
    public void bodyEqualToJson() throws Exception {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withBody(
                        equalToJson("{\"value1\":\"1\", \"value2\":\"2\"}")));
            }
        });
        Response response =
                with().body("{\"value1\":\"1\", \"value2\":\"2\"}").post(
                        SPY_SERVER_URL);
        response.then().statusCode(200).body(is(""));
        httpSpy.verify();
    }

    @Test
    public void bodyNotEqualToJson() throws Exception {
        httpSpy.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withBody(
                        equalToJson("{\"value1\":\"1\", \"value2\":\"2\"}")));
            }
        });
        Response response =
                with().body("{\"value1\":\"1\", \"value3\":\"3\"}").post(
                        SPY_SERVER_URL);
        response.then().statusCode(200).body(is(""));
        try {
            httpSpy.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports about JSON value mismatch",
                    e.getMessage(),
                    allOf(containsString("value1"), containsString("value2"),
                            containsString("value3")));
        }
    }

}
