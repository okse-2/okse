package no.ntnu.okse.protocol.mqtt;

import no.ntnu.okse.core.event.SubscriptionChangeEvent;
import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.AssertJUnit.assertEquals;

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
        assertEquals(false,subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing", clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing", clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
        assertEquals(1,subscriptionHandler_spy.getSubscriberIndexes(clientID).size());
    }
    @Test
    public void addSubscriber(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        assertEquals(false,subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing", clientID);
        assertEquals(true,subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
    }

    @Test
    public void removeSubscriber(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing", clientID);
        assertEquals(true, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
        subscriptionHandler_spy.removeSubscriber("127.0.0.1", 1883, "testing");
        assertEquals(false, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));

        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing", clientID);
        assertEquals(true, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
        subscriptionHandler_spy.removeSubscriber(subscriptionHandler_spy.getSubscriber("127.0.0.1", 1883, "testing").getSubscriber());
        assertEquals(false, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));

//        assertEquals(true, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
//        subscriptionHandler_spy.removeSubscriber(new Subscriber("localhost", 1234, "testing", "mqtt"));
//        assertEquals(true, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
    }

    @Test
    public void removeSubscribers(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing", clientID);
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1882, "testing2", clientID);
        subscriptionHandler_spy.addSubscriber("127.1.0.1", 1883, "testing", clientID + "2");

        assertEquals(true, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
        assertEquals(true, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1882, "testing2"));
        assertEquals(true, subscriptionHandler_spy.containsSubscriber("127.1.0.1", 1883, "testing"));
        subscriptionHandler_spy.removeSubscribers(clientID);
        assertEquals(false, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
        assertEquals(false, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1882, "testing2"));
        assertEquals(true, subscriptionHandler_spy.containsSubscriber("127.1.0.1", 1883, "testing"));
    }

    @Test
    public void containsSubscirbe(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing", clientID);
        assertEquals(true, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
        subscriptionHandler_spy.removeSubscriber("127.0.0.1", 1883, "testing");
        assertEquals(false, subscriptionHandler_spy.containsSubscriber("127.0.0.1", 1883, "testing"));
    }

    @Test
    public void getSubscriber(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing", clientID);
        MQTTSubscriber sub = subscriptionHandler_spy.getSubscriber("127.0.0.1", 1883, "testing");
        assertEquals("127.0.0.1", sub.getHost());
        assertEquals(1883, sub.getPort());
        assertEquals("testing", sub.getTopic());
        assertEquals(clientID, sub.getClientID());
    }

    @Test
    public void getSubscriberIndex(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing", clientID);
        int index = subscriptionHandler_spy.getSubscriberIndex("127.0.0.1", 1883, "testing");
        assertEquals(0, index);
    }

    @Test
    public void getSubscriberNonExistingSubscriber(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        MQTTSubscriber sub = subscriptionHandler_spy.getSubscriber("127.0.0.1", 1883, "testing");
        assertEquals(null, sub);
    }

    @Test
    public void getAllSubscribersFromTopic(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing", clientID);
        subscriptionHandler_spy.addSubscriber("127.1.0.1", 1883, "testing", clientID + "2");
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing2", clientID);
        ArrayList<MQTTSubscriber> subs = subscriptionHandler_spy.getAllSubscribersFromTopic("testing");
        assertEquals(2, subs.size());

        assertEquals("testing", subs.get(0).getTopic());
        assertEquals(clientID, subs.get(0).getClientID());
        assertEquals("testing", subs.get(1).getTopic());
        assertEquals(clientID + "2", subs.get(1).getClientID());
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

    @Test
    public void subscriptionChanged(){
        MQTTSubscriptionManager subscriptionManager = new MQTTSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        MQTTSubscriptionManager subscriptionHandler_spy = Mockito.spy(subscriptionManager);

        String clientID = "testClientID";
        subscriptionHandler_spy.addSubscriber("127.0.0.1", 1883, "testing", clientID);
        Subscriber sub = subscriptionHandler_spy.getSubscriber("127.0.0.1", 1883, "testing").getSubscriber();

        subscriptionHandler_spy.subscriptionChanged(new SubscriptionChangeEvent(SubscriptionChangeEvent.Type.UNSUBSCRIBE, sub));
        Mockito.verify(subscriptionHandler_spy).removeSubscriber(sub);
    }

    private MQTTSubscriptionManager getInstance(){
        return subscriptionManager;
    }
}
