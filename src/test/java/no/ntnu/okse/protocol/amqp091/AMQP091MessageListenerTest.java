package no.ntnu.okse.protocol.amqp091;

import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.joram.mom.amqp.messages.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;

@Test
public class AMQP091MessageListenerTest {
    private AMQP091MessageListener messageListener;
    @Mock
    private AMQP091ProtocolServer protocolServer;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private SubscriberMap subscriberMap;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        messageListener = new AMQP091MessageListener(protocolServer);
        messageListener.setSubscriberMap(subscriberMap);
        messageListener.setSubscriptionService(subscriptionService);
    }

    public void onConnect() {
        ConnectMessage connectMessage = new ConnectMessage("example.com", 1234, null);
        messageListener.onConnect(connectMessage);
        Mockito.verify(protocolServer, Mockito.times(1)).incrementTotalRequests();
    }

    public void onDisconnect() {
        DisconnectMessage disconnectMessage = new DisconnectMessage("example.com", 1234, null);
        List<Subscriber> subscribers = createSubscribers(3);
        Mockito.doReturn(subscribers).when(subscriberMap).getSubscribers(
                disconnectMessage.getHost(), disconnectMessage.getPort()
        );
        messageListener.onDisconnect(disconnectMessage);
        Mockito.verify(subscriptionService, Mockito.times(3)).removeSubscriber(any());
        Mockito.verify(protocolServer, Mockito.times(1)).incrementTotalRequests();
    }

    public void onMessageReceived() {
        byte[] body = "message".getBytes(StandardCharsets.UTF_8);
        MessageReceived message = new MessageReceived("topic1", "", body, "localhost", 1234);
        messageListener.onMessageReceived(message);
        Mockito.verify(protocolServer, Mockito.times(1)).incrementTotalMessagesReceived();
        Mockito.verify(protocolServer, Mockito.times(1)).incrementTotalRequests();
    }

    public void onSubscribe() {
        SubscribeMessage subscribeMessage = new SubscribeMessage("topic1", "", "localhost", 1234);
        messageListener.onSubscribe(subscribeMessage);
        Mockito.verify(subscriberMap, Mockito.times(1)).putSubscriber(any(Subscriber.class));
        Mockito.verify(subscriptionService, Mockito.times(1)).addSubscriber(any(Subscriber.class));
        Mockito.verify(protocolServer, Mockito.times(1)).incrementTotalRequests();
    }

    public void unUnsubscribe() {
        UnsubscribeMessage unsubscribeMessage = new UnsubscribeMessage("topic1", "", "localhost", 1234);
        messageListener.onUnsubscribe(unsubscribeMessage);
        Mockito.verify(subscriberMap, Mockito.times(1)).getSubscriber("localhost", 1234, "topic1");
        Mockito.verify(subscriptionService, Mockito.times(1)).removeSubscriber(any(Subscriber.class));
        Mockito.verify(protocolServer, Mockito.times(1)).incrementTotalRequests();
    }

    private List<Subscriber> createSubscribers(int num) {
        List<Subscriber> subscribers = new ArrayList<>();
        for(int i = 0; i < num; i++) {
            subscribers.add(createSubscriber());
        }
        return subscribers;
    }

    private Subscriber createSubscriber() {
        return new Subscriber("host", 1234, "topic", "protocol");
    }
}
