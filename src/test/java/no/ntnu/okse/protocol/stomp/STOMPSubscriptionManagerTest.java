package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.core.event.SubscriptionChangeEvent;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.mockito.Mockito;
import org.testng.annotations.*;

import java.util.HashMap;

import static org.testng.AssertJUnit.assertEquals;

public class STOMPSubscriptionManagerTest {
    private STOMPSubscriptionManager subscriptionHandler_spy;

    @BeforeMethod
    public void setUp() {
        STOMPSubscriptionManager subscriptionManager = new STOMPSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        subscriptionHandler_spy = Mockito.spy(subscriptionManager);
        //TODO: Remove this when we test init and change the getinstance method
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
    }

    @AfterMethod
    public void tearDown() {
        subscriptionHandler_spy = null;
    }

    @Test
    public void addSubscriber(){
        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "stomp");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void addExistingSubscriber(){
        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "stomp");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.removeSubscriber(clientID);
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void subscriptionChanged(){
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "stomp");
        SubscriptionChangeEvent e = new SubscriptionChangeEvent(SubscriptionChangeEvent.Type.UNSUBSCRIBE, sub);

        subscriptionHandler_spy.subscriptionChanged(e);
        Mockito.verify(subscriptionHandler_spy).removeSubscriber(sub);
    }

    @Test
    public void removeSubscriberSubscriber(){
        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "stomp");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.removeSubscriber(sub);
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void removeSubscriber(){
        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "stomp");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.removeSubscriber(clientID);
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void removeSubscriberWithHostPort(){
        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "stomp");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.removeSubscriber("127.0.0.1", 1883);
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void getAllSubscribersForTopic(){
        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "stomp");
        Subscriber sub2 = new Subscriber( "127.0.0.1", 1883, "testing", "stomp");
        Subscriber sub3 = new Subscriber( "127.0.0.1", 1883, "testing2", "stomp");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        subscriptionHandler_spy.addSubscriber(sub2, clientID + "2");

        HashMap<String, Subscriber> subs = subscriptionHandler_spy.getAllSubscribersForTopic("testing");
        assertEquals(2, subs.size());

        subscriptionHandler_spy.removeSubscriber(clientID);
        subs = subscriptionHandler_spy.getAllSubscribersForTopic("testing");
        assertEquals(1, subs.size());

        subscriptionHandler_spy.addSubscriber(sub3, clientID + "3");
        assertEquals(1, subs.size());
    }


}
