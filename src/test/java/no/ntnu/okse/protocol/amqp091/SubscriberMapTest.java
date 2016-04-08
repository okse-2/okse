package no.ntnu.okse.protocol.amqp091;

import no.ntnu.okse.core.subscription.Subscriber;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Test
public class SubscriberMapTest {

    private SubscriberMap subscriberMap;
    private int port = 12456;

    @BeforeMethod
    public void setUp() {
        subscriberMap = new SubscriberMap();
    }

    @AfterMethod
    public void tearDown() {
        subscriberMap = null;
    }

    public void putSubscriber_add() {
        Subscriber subscriber = generateSubscriber("Topic1");
        assertNull(getSubscriberFromSubscriber(subscriber));
        subscriberMap.putSubscriber(subscriber);
        assertEquals(getSubscriberFromSubscriber(subscriber), subscriber);
    }

    public void putSubscriber_multiple_subscribers() {
        Subscriber subscriber1 = generateNewSubscriber("Topic1");
        Subscriber subscriber2 = generateNewSubscriber("Topic1");
        Subscriber subscriber3 = generateNewSubscriber("Topic1");

        subscriberMap.putSubscriber(subscriber1);
        assertEquals(subscriber1, getSubscriberFromSubscriber(subscriber1));

        subscriberMap.putSubscriber(subscriber2);
        assertEquals(subscriber2, getSubscriberFromSubscriber(subscriber2));

        subscriberMap.putSubscriber(subscriber3);
        assertEquals(subscriber3, getSubscriberFromSubscriber(subscriber3));

        assertNull(getSubscriberFromSubscriber(generateSubscriber("Topic")));
    }

    public void putSubscriber_multiple_topics() {
        Subscriber topic1 = generateSubscriber("Topic1");
        Subscriber topic2 = generateSubscriber("Topic2");
        Subscriber topic3 = generateSubscriber("Topic3");

        assertEquals(0, subscriberMap.getSubscribers(topic1.getHost(), topic1.getPort()).size());
        subscriberMap.putSubscriber(topic1);
        assertEquals(1, subscriberMap.getSubscribers(topic1.getHost(), topic1.getPort()).size());
        subscriberMap.putSubscriber(topic2);
        assertEquals(2, subscriberMap.getSubscribers(topic1.getHost(), topic1.getPort()).size());
        subscriberMap.putSubscriber(topic3);
        assertEquals(3, subscriberMap.getSubscribers(topic1.getHost(), topic1.getPort()).size());
    }

    private Subscriber generateSubscriber(String topic) {
        return new Subscriber("localhost", port, topic, "TestProtocol");
    }

    private Subscriber generateNewSubscriber(String topic) {
        return new Subscriber("localhost", ++port, topic, "TestProtocol");
    }

    private Subscriber getSubscriberFromSubscriber(Subscriber subscriber) {
        return subscriberMap.getSubscriber(subscriber.getHost(), subscriber.getPort(), subscriber.getTopic());
    }
}
