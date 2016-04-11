package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.testng.AssertJUnit.assertEquals;

public class STOMPSubscriptionManagerTest {
    STOMPSubscriptionManager subscriptionManager;

    @BeforeTest
    public void setUp() {
        subscriptionManager = new STOMPSubscriptionManager();
        //TODO: Remove this when we test init and change the getinstance method
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
    }

    @AfterTest
    public void tearDown() {
        subscriptionManager = null;
    }

    @Test
    public void addSubscriber(){
        STOMPSubscriptionManager subscriptionManager = new STOMPSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        STOMPSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "mqtt");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void addExistingSubscriber(){
        STOMPSubscriptionManager subscriptionManager = new STOMPSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        STOMPSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "mqtt");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.removeSubscriber(clientID);
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void removeSubscriber(){
        STOMPSubscriptionManager subscriptionManager = new STOMPSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        STOMPSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "mqtt");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.removeSubscriber(clientID);
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void removeSubscriberWithHostPort(){
        STOMPSubscriptionManager subscriptionManager = new STOMPSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        STOMPSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "mqtt");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.removeSubscriber("127.0.0.1", 1883);
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void getAllSubscribersForTopic(){
        STOMPSubscriptionManager subscriptionManager = new STOMPSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        STOMPSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "mqtt");
        Subscriber sub2 = new Subscriber( "127.0.0.1", 1883, "testing", "mqtt");
        Subscriber sub3 = new Subscriber( "127.0.0.1", 1883, "testing2", "mqtt");
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
