package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.send.SendMessage;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import no.ntnu.okse.protocol.stomp.STOMPSubscriptionManager;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class SubscriptionListenerTest {
    private STOMPProtocolServer ps;
    private STOMPProtocolServer ps_spy;
    private SubscriptionListener listener;
    private SubscriptionListener listener_spy;
    private STOMPSubscriptionManager subscritpionManager_spy;

    @BeforeTest
    public void setUp() {
        listener = new SubscriptionListener();
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
        assertEquals(StompMessageType.SUBSCRIBE, types[0]);
    }

    @Test
    public void messageReceived() throws Exception {
        StampyMessage msg = createSubMessage();
        HostPort hostport = createHostPort();
        listener_spy.messageReceived(msg, hostport);

        ArgumentCaptor<Subscriber> subscriberArgument = ArgumentCaptor.forClass(Subscriber.class);
        ArgumentCaptor<String> clientIDArgument = ArgumentCaptor.forClass(String.class);
        Mockito.verify(subscritpionManager_spy).addSubscriber(subscriberArgument.capture(), clientIDArgument.capture());
        assertEquals( "ogdans3", clientIDArgument.getValue());
        assertEquals( "bernt", subscriberArgument.getValue().getTopic());
    }

    private HostPort createHostPort(){
        return new HostPort("localhost", 61613);
    }

    private StampyMessage createSubMessage(){
        SubscribeMessage msg = new SubscribeMessage();
        msg.getHeader().setDestination("bernt");
        msg.getHeader().setId("ogdans3");
        return msg;
    }

}
