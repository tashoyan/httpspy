package org.httpspy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CamelJettyHttpServerMockStateTest {

    private static final int MOCK_SERVER_PORT = 47604;

    private static final String MOCK_SERVER_PATH = "/mockseverpath/";

    private CamelJettyHttpServerMock httpServerMock;

    @Before
    public void initServerMock() {
        this.httpServerMock =
                new CamelJettyHttpServerMock(MOCK_SERVER_PORT, MOCK_SERVER_PATH);
    }

    @After
    public void stopServerMock() {
        httpServerMock.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void startAlreadyStarted() {
        httpServerMock.start();
        httpServerMock.start();
    }

    @Test
    public void stopAlreadyStopped() {
        httpServerMock.start();
        httpServerMock.stop();
        httpServerMock.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void setServiceThreadsNumberAlreadyStarted() {
        httpServerMock.start();
        httpServerMock.setServiceThreadsNumber(10);
    }
}
