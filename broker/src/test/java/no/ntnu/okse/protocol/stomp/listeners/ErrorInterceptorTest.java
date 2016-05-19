package no.ntnu.okse.protocol.stomp.listeners;

import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ErrorInterceptorTest {
    ErrorInterceptor interceptor;
    STOMPProtocolServer ps;

    @BeforeTest
    public void setup(){
        interceptor = Mockito.spy(new ErrorInterceptor());
        ps = Mockito.spy(new STOMPProtocolServer("localhost", 1883));
        interceptor.setProtocolServer(ps);
    }

    @AfterTest
    public void tearDown(){
        interceptor = null;
        ps = null;
    }

    @Test
    public void onError(){
        interceptor.onError(null);
        Mockito.verify(ps).incrementTotalErrors();
    }
}
