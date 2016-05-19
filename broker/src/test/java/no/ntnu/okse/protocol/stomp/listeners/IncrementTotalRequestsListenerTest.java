package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class IncrementTotalRequestsListenerTest {
    private STOMPProtocolServer ps;
    private IncrementTotalRequestsListener listener;
    private STOMPProtocolServer ps_spy;
    private IncrementTotalRequestsListener listener_spy;

    @BeforeTest
    public void setUp() {
        listener = new IncrementTotalRequestsListener();
        ps = new STOMPProtocolServer("localhost", 61613);

        ps_spy = Mockito.spy(ps);

        listener.setProtocolServer(ps_spy);

        listener_spy = Mockito.spy(listener);
    }

    @AfterTest
    public void tearDown() {
        listener = null;
        listener_spy = null;
        ps_spy = null;
        ps = null;
    }

    @Test
    public void isForMessage(){
        assertEquals(true, listener_spy.isForMessage(null));
    }

    @Test
    public void getMessageTypes(){
        StompMessageType[] types = listener_spy.getMessageTypes();
        assertEquals(StompMessageType.values().length, types.length);
    }

    @Test
    public void messageReceived() throws Exception {
        listener_spy.messageReceived(null, null);
        Mockito.verify(ps_spy).incrementTotalRequests();
    }


}
