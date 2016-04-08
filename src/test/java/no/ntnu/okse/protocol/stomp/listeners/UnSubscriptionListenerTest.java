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
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

public class UnSubscriptionListenerTest {
    private STOMPProtocolServer ps;
    private STOMPProtocolServer ps_spy;
    private UnSubscriptionListener listener;
    private UnSubscriptionListener listener_spy;
    private STOMPSubscriptionManager subscritpionManager_spy;

    @BeforeTest
    public void setUp() {
        listener = new UnSubscriptionListener();
        ps = new STOMPProtocolServer("localhost", 61613);
        STOMPSubscriptionManager subscriptionManager = new STOMPSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        subscritpionManager_spy = Mockito.spy(subscriptionManager);

        listener.setSubscriptionManager(subscritpionManager_spy);

        ps_spy = Mockito.spy(ps);
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
        assertEquals(StompMessageType.UNSUBSCRIBE, types[0]);
    }

    @Test
    public void messageReceived() throws Exception {
        listener_spy.messageReceived(createUnsubMessage(), createHostPort());
        Mockito.verify(subscritpionManager_spy).removeSubscriber("ogdans3");
    }

    private HostPort createHostPort(){
        return new HostPort("localhost", 61613);
    }

    private StampyMessage createUnsubMessage(){
        UnsubscribeMessage msg = new UnsubscribeMessage();
        msg.getHeader().setId("ogdans3");
        return msg;
    }


}