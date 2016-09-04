package org.httpspy;

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
