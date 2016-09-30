# HTTP Spy

HTTP Spy is a spy tool for unit tests on HTTP clients.

_For definition of test spy and how it differs from mock object, see
[xUnit Patters web page](http://xunitpatterns.com/Mocks,%20Fakes,%20Stubs%20and%20Dummies.html)._

When setting test expectations, you program HTTP Spy what HTTP requests to
expect. For example, you can set up expectations on the following client
actions:

* Total number of requests
* Order of requests
* Request body, headers
* Request method (POST/GET etc.)

In addition, you may instruct HTTP Spy which response it should send on each
incoming request. After expectations are set, you start HTTP Spy and run your
HTTP client under test. Finally, you instruct HTTP Spy to verify all requests
it received.

HTTP Spy runs on a local network interface. It is possible to configure network
port and HTTP path where HTTP Spy service incoming requests. You can also
specify the number of servicing threads. However, if you configured more than
one servicing thread in HTTP Spy, then you cannot set expectations on request
order.

HTTP Spy provides the following ways to set expectations on body and header
values, as well as on HTTP method and path:

* String is equal to the expected value
* String is equal ignoring case
* String matches by means of [Hamcrest matcher](http://hamcrest.org/)
* XML is identical
* JSON is identical

This version of HTTP Spy is implemented with [Camel Jetty](http://camel.apache.org/jetty.html).

## Usage

First, you need to declare a Maven dependency on HTTP Spy:

    <groupId>com.github.tashoyan.httpspy</groupId>
    <artifactId>httpspy</artifactId>
    <version>1.0</version>
    <scope>test</scope>

The dependency scope is `test` because normally you use HTTP Spy in unit tests
only.

Start HTTP Spy instance on `0.0.0.0` network interface, on port number `47604`
and let it service requests on HTTP path `/spyseverpath`:

    HttpSpy httpSpy = CamelJettyHttpSpy("0.0.0.0", 47604, "/spyseverpath");
    httpSpy.start();

Setup some expectations:

    httpSpy.expectRequests((new AbstractRequestExpectationListBuilder() {
        public void build() {
            expect(request()
                         .withBody(matching(CoreMatchers.containsString("Hello")))
                         .withHeader("h1", 0, equalTo("v1"))
                         .withMethod(equalTo("POST"))
                         .withPath(equalTo("/path/"))
                         .andResponse(response()
                             .withStatus(200)
                             .withBody("OK")
                             .withHeader("h2", "v2")));
            expect(request()
                         .withBody(equalToXml(myXmlSample))
                         .andResponse(response()
                             .withStatus(500)
                             .withBody("Cannot help")
                             .withDelay(TimeUnit.MILLISECONDS, 1000)));
         }
    });

Here we expect two requests to come. The first request is expected to have
string `Hello` in its body. It also should have a header `h1` with the first
value equal to `v1`. We also expect it to be `POST` request coming on HTTP path
`/path/`. Finally, we ask HTTP Spy to send an `OK` response.
The second request is expected to have an XML body identical to some sample. We
ask HTTP Spy to respond with `500` error.

As soon as HTTP Spy is up and has expectations, you are ready to run your HTTP client
and let it send requests. After client finished, you ask HTTP Spy to verify
requests it has received:

    ...
    // Client under test executes requests...
    ...
    httpSpy.verify();

HTTP Spy will fail the test if it finds that some expectations on client
request are not met.

If you don't need HTTP Spy anymore, then you need to stop it to free up
resources it consumes:

    httpSpy.stop();

Alternatively, you can reset HTTP Spy expectations and continue using the same
instance in the next test:

    httpSpy.reset();

## Additional documentation

For configuration options, see Javadoc of methods in `HttpSpy` interface.

For different options to set request expectations, see Javadoc of methods in
`AbstractRequestExpectationListBuilder` class.

For response options, see Javadoc of methods in `ResponseBuilder` interface.
