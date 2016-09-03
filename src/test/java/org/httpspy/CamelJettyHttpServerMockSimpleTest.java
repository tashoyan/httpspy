package org.httpspy;

import static com.jayway.restassured.RestAssured.with;
import com.jayway.restassured.response.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.*;
import org.junit.After;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class CamelJettyHttpServerMockSimpleTest {

    private static final String MOCK_SERVER_HOST = "0.0.0.0";

    private static final int MOCK_SERVER_PORT = 47604;

    private static final String MOCK_SERVER_PATH = "/mockseverpath/";

    private static final String MOCKSERVER_URL = "http://"
            + MOCK_SERVER_HOST + ":" + MOCK_SERVER_PORT + MOCK_SERVER_PATH;

    private CamelJettyHttpServerMock httpServerMock;

    @Before
    public void before() throws Exception {
        httpServerMock =
                new CamelJettyHttpServerMock(MOCK_SERVER_HOST, MOCK_SERVER_PORT,
                        MOCK_SERVER_PATH);
        httpServerMock.start();
    }

    @After
    public void after() throws Exception {
        httpServerMock.stop();
    }

    @Test
    public void oneRequestWithResponse() throws Exception {
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request()
                        .withBody(equalTo("<body>Hello</body>"))
                        .withHeader("h1", 0, equalTo("v1"))
                        .withHeader("h2", 0, equalToIgnoreCase("v2"))
                        .withMethod(equalTo("POST"))
                        .withPath(equalTo(MOCK_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK")
                                        .withHeader("h2", "v2")));
            }
        });
        Map<String, Object> headers = new HashMap<>();
        headers.put("h1", "v1");
        headers.put("h2", "V2");
        Response response =
                with().body("<body>Hello</body>").headers(headers)
                        .post(MOCKSERVER_URL);
        response.then().statusCode(200).body(is("OK")).header("h2", "v2");
        httpServerMock.verify();
    }

    @Test
    public void oneRequestWithoutResponse() throws Exception {
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withBody(equalTo("<body>Hello</body>"))
                        .withHeader("h1", equalTo("v1")).withMethod(equalTo("POST"))
                        .withPath(equalTo(MOCK_SERVER_PATH)));
            }
        });
        Response response =
                with().body("<body>Hello</body>")
                        .headers(Collections.singletonMap("h1", "v1"))
                        .post(MOCKSERVER_URL);
        response.then().statusCode(200).body(is(""));
        httpServerMock.verify();
    }

    @Test
    public void emptyRequestExpectation() throws Exception {
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request());
            }
        });
        Response response =
                with().body("<body>Hello</body>")
                        .headers(Collections.singletonMap("h1", "v1"))
                        .post(MOCKSERVER_URL);
        response.then().statusCode(200).body(is(""));
        httpServerMock.verify();
    }

    @Test
    public void bodyEqualToXml() throws Exception {
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withBody(equalToXml("<xml><color>red</color></xml>")));
            }
        });
        Response response =
                with().body("<xml><color>red</color></xml>").post(MOCKSERVER_URL);
        response.then().statusCode(200).body(is(""));
        httpServerMock.verify();
    }

    @Test
    public void bodyNotEqualToXml() throws Exception {
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withBody(equalToXml("<xml><color>red</color></xml>")));
            }
        });
        Response response =
                with().body("<xml><color>green</color></xml>").post(MOCKSERVER_URL);
        response.then().statusCode(200).body(is(""));
        try {
            httpServerMock.verify();
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
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withBody(
                        equalToJson("{\"value1\":\"1\", \"value2\":\"2\"}")));
            }
        });
        Response response =
                with().body("{\"value1\":\"1\", \"value2\":\"2\"}").post(
                        MOCKSERVER_URL);
        response.then().statusCode(200).body(is(""));
        httpServerMock.verify();
    }

    @Test
    public void bodyNotEqualToJson() throws Exception {
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withBody(
                        equalToJson("{\"value1\":\"1\", \"value2\":\"2\"}")));
            }
        });
        Response response =
                with().body("{\"value1\":\"1\", \"value3\":\"3\"}").post(
                        MOCKSERVER_URL);
        response.then().statusCode(200).body(is(""));
        try {
            httpServerMock.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat(
                    "Error message reports about JSON value mismatch",
                    e.getMessage(),
                    allOf(containsString("value1"), containsString("value2"),
                            containsString("value3")));
        }
    }

    @Test
    public void unexpectedHeaderValue() throws Exception {
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withHeader("h1", equalTo("v1")).andResponse(
                        response().withStatus(200)));
            }
        });
        Response response =
                with().body("<body>Hello</body>")
                        .headers(Collections.singletonMap("h1", "unexpected_value"))
                        .post(MOCKSERVER_URL);
        response.then().statusCode(200);
        try {
            httpServerMock.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat("Error message reports about unexpected header value",
                    e.getMessage(),
                    both(containsString("h1")).and(containsString("unexpected_value")));
        }
    }

    @Test
    public void withHeaderAnyValue() {
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withHeader("h1"));
                expect(request().withHeader("h2"));
            }
        });
        Response response =
                with().body("<body>Hello</body>")
                        .headers(Collections.singletonMap("h1", "v1"))
                        .post(MOCKSERVER_URL);
        response.then().statusCode(200);
        response = with().body("<body>Hello 2</body>").post(MOCKSERVER_URL);
        response.then().statusCode(200);
        try {
            httpServerMock.verify();
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
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withoutHeader("h1").andResponse(
                        response().withStatus(200)));
            }
        });
        Response response =
                with().body("<body>Hello</body>")
                        .headers(Collections.singletonMap("h1", "without_header"))
                        .post(MOCKSERVER_URL);
        response.then().statusCode(200);
        try {
            httpServerMock.verify();
            fail("AssertionError expected");
        } catch (AssertionError e) {
            assertThat("Error message reports that request should be without header",
                    e.getMessage(),
                    both(containsString("h1")).and(containsString("without_header")));
        }
    }

    @Test
    public void strictHeadersUnexpectedHeader() throws Exception {
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().withStrictHeaders().withHeader("h1", equalTo("v1"))
                        .andResponse(response().withStatus(200)));
            }
        });
        Map<String, Object> headers = new HashMap<>();
        headers.put("h1", "v1");
        headers.put("unexpected_header", "v2");
        Response response =
                with().body("<body>Hello</body>").headers(headers)
                        .post(MOCKSERVER_URL);
        response.then().statusCode(200);
        try {
            httpServerMock.verify();
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
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request().andResponse(
                        response().withStatus(200).withHeader("h1", "v1")
                                .withHeader("h1", "v2").withHeader("h1", "v3")));
            }
        });
        Response response = with().body("<body>Hello</body>").post(MOCKSERVER_URL);
        response.then().statusCode(200).header("h1", "v1,v2,v3");
        httpServerMock.verify();
    }

    @Test
    public void manyRequestsAllWithResponses() throws Exception {
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("1"))))
                        .withHeader("request", matching(endsWith("1")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(MOCK_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK1")
                                        .withHeader("reply", "v1")));
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("2"))))
                        .withHeader("request", matching(endsWith("2")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(MOCK_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK2")
                                        .withHeader("reply", "v2")));
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("3"))))
                        .withHeader("request", matching(endsWith("3")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(MOCK_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK3")
                                        .withHeader("reply", "v3")));
            }
        });
        int requestsNumber = 3;
        for (int i = 1; i <= requestsNumber; i++) {
            Response response = with().body("body"
                    + i).headers(Collections.singletonMap("request", "v"
                    + i)).post(MOCKSERVER_URL);
            response.then().statusCode(200).body(is("OK"
                    + i)).header("reply", "v"
                    + i);
        }
        httpServerMock.verify();
    }

    @Test
    public void manyRequestsSomeWithResponses() throws Exception {
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("1"))))
                        .withHeader("request", matching(endsWith("1")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(MOCK_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK1")
                                        .withHeader("reply", "v1")));
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("2"))))
                        .withHeader("request", matching(endsWith("2")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(MOCK_SERVER_PATH)));
                expect(request()
                        .withBody(
                                matching(both(containsString("body")).and(
                                        containsString("3"))))
                        .withHeader("request", matching(endsWith("3")))
                        .withMethod(matching(startsWith("PO")))
                        .withPath(equalTo(MOCK_SERVER_PATH))
                        .andResponse(
                                response().withStatus(200).withBody("OK3")
                                        .withHeader("reply", "v3")));
            }
        });
        int requestsNumber = 3;
        for (int i = 1; i <= requestsNumber; i++) {
            Response response = with().body("body"
                    + i).headers(Collections.singletonMap("request", "v"
                    + i)).post(MOCKSERVER_URL);
            response.then().statusCode(200);
            if (i == 2) {
                response.then().body(is("")).header("reply", nullValue());
            } else {
                response.then().body(is("OK"
                        + i)).header("reply", "v"
                        + i);
            }
        }
        httpServerMock.verify();
    }

    @Test
    public void manyRequestsWithResponsesManyTimes() throws Exception {
        int requestsNumber = 10;
        httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

            @Override
            public void build() {
                expect(requestsNumber,
                        request()
                                .withBody(equalTo("<body>Hello</body>"))
                                .withHeader("request")
                                .withMethod(equalTo("POST"))
                                .withPath(equalTo(MOCK_SERVER_PATH))
                                .andResponse(
                                        response().withStatus(200).withBody("OK")
                                                .withHeader("reply", "v")));
            }
        });
        for (int i = 0; i < requestsNumber; i++) {
            Response response =
                    with().body("<body>Hello</body>")
                            .headers(Collections.singletonMap("request", "v"))
                            .post(MOCKSERVER_URL);
            response.then().statusCode(200).body(is("OK")).header("reply", "v");
        }
        httpServerMock.verify();
    }

    @Test
    public void useSameMockServerServeralTimes() throws Exception {
        int reuseNumber = 5;
        for (int i = 0; i < reuseNumber; i++) {
            httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

                @Override
                public void build() {
                    expect(request());
                }
            });
            Response response = with().body("request-"
                    + i).post(MOCKSERVER_URL);
            response.then().statusCode(200);
            httpServerMock.verify();
            httpServerMock.reset();
        }
    }

    @Test
    public void expectRequestsSeveralTimes() {
        int expectRequestsNumber = 5;
        for (int i = 0; i < expectRequestsNumber; i++) {
            String expectedBody = "request-"
                    + i;
            String responseBody = "response-"
                    + i;
            httpServerMock.expectRequests(new AbstractRequestExpectationListBuilder() {

                @Override
                public void build() {
                    expect(request().withBody(equalTo(expectedBody)).andResponse(
                            response().withBody(responseBody)));
                }
            });
        }
        for (int i = 0; i < expectRequestsNumber; i++) {
            Response response = with().body("request-"
                    + i).post(MOCKSERVER_URL);
            response.then().statusCode(200).body(is("response-"
                    + i));
        }
    }
}
