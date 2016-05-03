package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.server.message.error.ErrorMessage;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class ErrorListenerTest {
    private ErrorListener listener;
    private ErrorListener listener_spy;
    private AbstractStampyMessageGateway gateway_spy;
    private STOMPProtocolServer ps;
    private STOMPProtocolServer ps_spy;

    @BeforeTest
    public void setUp() throws Exception {
        listener = new ErrorListener();
        ps = new STOMPProtocolServer("localhost", 61613);

        ps_spy = Mockito.spy(ps);

        listener.setProtocolServer(ps_spy);

        listener_spy = Mockito.spy(listener);
    }

    @AfterTest
    public void tearDown() throws Exception {
        if(gateway_spy != null)
            gateway_spy.shutdown();
        gateway_spy = null;
        listener = null;
        listener_spy = null;
    }

    @Test
    public void isForMessage(){
        assertEquals(true, listener_spy.isForMessage(null));
    }

    @Test
    public void getMessageTypes(){
        StompMessageType[] types = listener_spy.getMessageTypes();
        assertEquals(StompMessageType.ERROR, types[0]);
    }

    @Test
    public void messageReceived() throws Exception {
        StampyMessage msg = createSendMessage();
        HostPort hostPort = new HostPort("localhost", 61613);

        listener_spy.interceptMessage(msg);
        Mockito.verify(ps_spy).incrementTotalBadRequests();
        Mockito.reset(listener_spy);
        Mockito.reset(ps_spy);

        listener_spy.interceptMessage(msg, hostPort);
        Mockito.verify(ps_spy).incrementTotalBadRequests();
        Mockito.reset(listener_spy);
        Mockito.reset(ps_spy);
    }

    private StampyMessage createSendMessage(){
        return new ErrorMessage();
    }
}
