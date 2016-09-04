package org.httpspy;

import static org.junit.Assert.*;
import org.junit.Test;

public class CamelJettyHttpSpyConstructTest {

    private CamelJettyHttpSpy httpSpy;

    @Test(expected = NullPointerException.class)
    public void nullHostname() {
        httpSpy = new CamelJettyHttpSpy(null, 5, "path");
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyHostname() {
        httpSpy = new CamelJettyHttpSpy("", 5, "path");
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankHostname() {
        httpSpy = new CamelJettyHttpSpy("  ", 5, "path");
    }

    @Test
    public void nonBlankHostname() {
        httpSpy = new CamelJettyHttpSpy("hostname", 5, "path");
        assertEquals("Hostname", "hostname", httpSpy.getHostname());
    }

    @Test
    public void defaultHostname() {
        httpSpy = new CamelJettyHttpSpy(5, "path");
        assertEquals("Hostname", "localhost", httpSpy.getHostname());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativePort() {
        httpSpy = new CamelJettyHttpSpy(-5, "A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroPort() {
        httpSpy = new CamelJettyHttpSpy(0, "A");
    }

    @Test
    public void positivePort() {
        httpSpy = new CamelJettyHttpSpy(5, "A");
        assertEquals("Port", 5, httpSpy.getPort());
    }

    @Test
    public void nullPath() {
        httpSpy = new CamelJettyHttpSpy(5, null);
        assertEquals("Path", "/", httpSpy.getPath());
    }

    @Test
    public void emptyPath() {
        httpSpy = new CamelJettyHttpSpy(5, "");
        assertEquals("Path", "/", httpSpy.getPath());
    }

    @Test
    public void nonEmptyPath() {
        httpSpy = new CamelJettyHttpSpy(5, "pa/th");
        assertEquals("Path", "/pa/th/", httpSpy.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonEmptyPathWithSpace() {
        httpSpy = new CamelJettyHttpSpy(5, "pa/ th");
    }

    @Test
    public void nonEmptyPathStartsWithSlash() {
        httpSpy = new CamelJettyHttpSpy(5, "/pa/th");
        assertEquals("Path", "/pa/th/", httpSpy.getPath());
    }

    @Test
    public void nonEmptyPathEndsWithSlash() {
        httpSpy = new CamelJettyHttpSpy(5, "pa/th/");
        assertEquals("Path", "/pa/th/", httpSpy.getPath());
    }

    @Test
    public void defaultServiceThreadsNumber() {
        httpSpy = new CamelJettyHttpSpy(13000, "pa/th");
        assertEquals("Service threads number", 1, httpSpy.getServiceThreadsNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeServiceThreadsNumber() {
        httpSpy = new CamelJettyHttpSpy(13000, "pa/th");
        httpSpy.setServiceThreadsNumber(-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroServiceThreadsNumber() {
        httpSpy = new CamelJettyHttpSpy(13000, "pa/th");
        httpSpy.setServiceThreadsNumber(0);
    }

    @Test
    public void positiveServiceThreadsNumber() {
        httpSpy = new CamelJettyHttpSpy(13000, "pa/th");
        httpSpy.setServiceThreadsNumber(10);
        assertEquals("Service threads number", 10, httpSpy.getServiceThreadsNumber());
    }
}
