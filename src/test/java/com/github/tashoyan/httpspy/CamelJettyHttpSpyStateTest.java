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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CamelJettyHttpSpyStateTest {

    private static final int SPY_SERVER_PORT = 47604;

    private static final String SPY_SERVER_PATH = "/spyseverpath/";

    private CamelJettyHttpSpy httpSpy;

    @Before
    public void initSpyServer() {
        this.httpSpy = new CamelJettyHttpSpy(SPY_SERVER_PORT, SPY_SERVER_PATH);
    }

    @After
    public void stopSpyServer() {
        httpSpy.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void startAlreadyStarted() {
        httpSpy.start();
        httpSpy.start();
    }

    @Test
    public void stopAlreadyStopped() {
        httpSpy.start();
        httpSpy.stop();
        httpSpy.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void setServiceThreadsNumberAlreadyStarted() {
        httpSpy.start();
        httpSpy.setServiceThreadsNumber(10);
    }
}
