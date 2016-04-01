package no.ntnu.okse.protocol.mqtt;

import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by ogdans3 on 3/16/16.
 */
public class MQTTSubscriptionManagerTest {

    MQTTSubscriptionManager subscriptionManager;

    @BeforeTest
    public void setUp() {
        subscriptionManager = new MQTTSubscriptionManager();
        //TODO: Remove this when we test init and change the getinstance method
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
    }

    @AfterTest
    public void tearDown() {
        subscriptionManager = null;
    }

    @Test
    public void addExistingSubscriber(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "mqtt");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
    }
    @Test
    public void addSubscriber(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "mqtt");
        assertEquals(false,subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void removeSubscriber(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "mqtt");
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true, subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.removeSubscriber(clientID);
        assertEquals(false, subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void containsSubscirbe(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "mqtt");
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(true, subscriptionHandler_spy.containsSubscriber(clientID));
        subscriptionHandler_spy.removeSubscriber(clientID);
        assertEquals(false, subscriptionHandler_spy.containsSubscriber(clientID));
    }

    @Test
    public void getSubscriber(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Subscriber sub = new Subscriber( "127.0.0.1", 1883, "testing", "mqtt");
        subscriptionHandler_spy.addSubscriber(sub, clientID);
        assertEquals(sub, subscriptionHandler_spy.getSubscriber(clientID));
    }

    @Test
    public void addPublisher(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Publisher pub = new Publisher( "testing", "127.0.0.1", 1883, "MQTT");
        assertEquals(false, subscriptionHandler_spy.containsPublisher(clientID));
        subscriptionHandler_spy.addPublisher(pub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsPublisher(clientID));
    }

    @Test
    public void addExistingPublisher(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Publisher pub = new Publisher( "testing", "127.0.0.1", 1883, "MQTT");
        assertEquals(false, subscriptionHandler_spy.containsPublisher(clientID));
        subscriptionHandler_spy.addPublisher(pub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsPublisher(clientID));
        assertEquals(true, subscriptionHandler_spy.containsPublisher(clientID));
        subscriptionHandler_spy.addPublisher(pub, clientID);
        assertEquals(true,subscriptionHandler_spy.containsPublisher(clientID));
    }

    @Test
    public void removePublisher(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Publisher pub = new Publisher( "testing", "127.0.0.1", 1883, "MQTT");
        subscriptionHandler_spy.addPublisher(pub, clientID);
        assertEquals(true, subscriptionHandler_spy.containsPublisher(clientID));
        subscriptionHandler_spy.removePublisher(clientID);
        assertEquals(false, subscriptionHandler_spy.containsPublisher(clientID));
    }

    @Test
    public void containsPublisher(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Publisher pub = new Publisher( "testing", "127.0.0.1", 1883, "MQTT");
        subscriptionHandler_spy.addPublisher(pub, clientID);
        assertEquals(true, subscriptionHandler_spy.containsPublisher(clientID));
        subscriptionHandler_spy.removePublisher(clientID);
        assertEquals(false, subscriptionHandler_spy.containsPublisher(clientID));
    }



    @Test
    public void getPublisher(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        Publisher pub = new Publisher( "testing", "127.0.0.1", 1883, "MQTT");
        subscriptionHandler_spy.addPublisher(pub, clientID);
        assertEquals(pub, subscriptionHandler_spy.getPublisher(clientID));
    }

    private MQTTSubscriptionManager getInstance(){
        return subscriptionManager;
    }
}
