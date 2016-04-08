package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import no.ntnu.okse.protocol.stomp.STOMPSubscriptionManager;
import org.mockito.Mockito;
import org.testng.annotations.*;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

public class IDontNeedSecurityTest{
    private STOMPProtocolServer ps;
    private STOMPProtocolServer ps_spy;
    private IDontNeedSecurity listener;
    private IDontNeedSecurity listener_spy;
    private STOMPSubscriptionManager subscritpionManager_spy;

    @BeforeMethod
    public void setUp() {
        listener = new IDontNeedSecurity();
        listener_spy = Mockito.spy(listener);
    }

    @AfterMethod
    public void tearDown() {
        listener = null;
        listener_spy = null;
    }

    @Test
    public void isForMessage(){
        assertEquals(false, listener_spy.isForMessage(null));
    }

    @Test
    public void getMessageTypes(){
        StompMessageType[] types = listener_spy.getMessageTypes();
        assertEquals(null, types);
    }

    @Test
    public void messageReceived() throws Exception {
        listener_spy.messageReceived(null, null);
    }
}