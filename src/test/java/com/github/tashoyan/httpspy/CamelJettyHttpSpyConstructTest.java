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

import com.github.tashoyan.httpspy.CamelJettyHttpSpy;
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
