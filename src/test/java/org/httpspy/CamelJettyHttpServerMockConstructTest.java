package org.httpspy;

import static org.junit.Assert.*;
import org.junit.Test;

public class CamelJettyHttpServerMockConstructTest {

    private CamelJettyHttpServerMock httpServerMock;

    @Test(expected = NullPointerException.class)
    public void nullHostname() {
        httpServerMock = new CamelJettyHttpServerMock(null, 5, "path");
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyHostname() {
        httpServerMock = new CamelJettyHttpServerMock("", 5, "path");
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankHostname() {
        httpServerMock = new CamelJettyHttpServerMock("  ", 5, "path");
    }

    @Test
    public void nonBlankHostname() {
        httpServerMock = new CamelJettyHttpServerMock("hostname", 5, "path");
        assertEquals("Hostname", "hostname", httpServerMock.getHostname());
    }

    @Test
    public void defaultHostname() {
        httpServerMock = new CamelJettyHttpServerMock(5, "path");
        assertEquals("Hostname", "localhost", httpServerMock.getHostname());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativePort() {
        httpServerMock = new CamelJettyHttpServerMock(-5, "A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroPort() {
        httpServerMock = new CamelJettyHttpServerMock(0, "A");
    }

    @Test
    public void positivePort() {
        httpServerMock = new CamelJettyHttpServerMock(5, "A");
        assertEquals("Port", 5, httpServerMock.getPort());
    }

    @Test
    public void nullPath() {
        httpServerMock = new CamelJettyHttpServerMock(5, null);
        assertEquals("Path", "/", httpServerMock.getPath());
    }

    @Test
    public void emptyPath() {
        httpServerMock = new CamelJettyHttpServerMock(5, "");
        assertEquals("Path", "/", httpServerMock.getPath());
    }

    @Test
    public void nonEmptyPath() {
        httpServerMock = new CamelJettyHttpServerMock(5, "pa/th");
        assertEquals("Path", "/pa/th/", httpServerMock.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonEmptyPathWithSpace() {
        httpServerMock = new CamelJettyHttpServerMock(5, "pa/ th");
    }

    @Test
    public void nonEmptyPathStartsWithSlash() {
        httpServerMock = new CamelJettyHttpServerMock(5, "/pa/th");
        assertEquals("Path", "/pa/th/", httpServerMock.getPath());
    }

    @Test
    public void nonEmptyPathEndsWithSlash() {
        httpServerMock = new CamelJettyHttpServerMock(5, "pa/th/");
        assertEquals("Path", "/pa/th/", httpServerMock.getPath());
    }

    @Test
    public void defaultServiceThreadsNumber() {
        httpServerMock = new CamelJettyHttpServerMock(13000, "pa/th");
        assertEquals("Service threads number", 1,
                httpServerMock.getServiceThreadsNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeServiceThreadsNumber() {
        httpServerMock = new CamelJettyHttpServerMock(13000, "pa/th");
        httpServerMock.setServiceThreadsNumber(-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroServiceThreadsNumber() {
        httpServerMock = new CamelJettyHttpServerMock(13000, "pa/th");
        httpServerMock.setServiceThreadsNumber(0);
    }

    @Test
    public void positiveServiceThreadsNumber() {
        httpServerMock = new CamelJettyHttpServerMock(13000, "pa/th");
        httpServerMock.setServiceThreadsNumber(10);
        assertEquals("Service threads number", 10,
                httpServerMock.getServiceThreadsNumber());
    }
}
