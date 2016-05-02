package no.ntnu.okse.protocol.amqp091;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.apache.log4j.Logger;
import org.ow2.joram.mom.amqp.AMQPMessageListener;
import org.ow2.joram.mom.amqp.messages.*;

import java.util.List;

/**
 * AMQP 0.9.1 message listener
 */
public class AMQP091MessageListener implements AMQPMessageListener {

    private static Logger log = Logger.getLogger(AMQP091MessageListener.class);

    private final AMQP091ProtocolServer amqpProtocolServer;
    private SubscriberMap subscriberMap;
    private SubscriptionService subscriptionService;

    /**
     * Constructor with dependency injection for protocol server
     *
     * @param amqp091ProtocolServer AMQP 0.9.1 protocol server
     */
    public AMQP091MessageListener(AMQP091ProtocolServer amqp091ProtocolServer) {
        this.amqpProtocolServer = amqp091ProtocolServer;
        subscriberMap = new SubscriberMap();
        subscriptionService = SubscriptionService.getInstance();
    }

    /**
     * Event handler for connect events
     *
     * @param connectMessage connect message
     */
    @Override
    public void onConnect(ConnectMessage connectMessage) {
        log.debug(String.format("(%s:%d) connected",
                connectMessage.getHost(), connectMessage.getPort()
        ));
        amqpProtocolServer.incrementTotalRequests();
    }

    /**
     * Event handler for disconnect events
     *
     * @param disconnectMessage disconnect message
     */
    @Override
    public void onDisconnect(DisconnectMessage disconnectMessage) {
        log.debug(String.format("(%s:%d) disconnected",
                disconnectMessage.getHost(), disconnectMessage.getPort()
        ));
        List<Subscriber> subscribers = subscriberMap.getSubscribers(disconnectMessage.getHost(), disconnectMessage.getPort());
        for(Subscriber subscriber : subscribers) {
            subscriptionService.removeSubscriber(subscriber);
        }
        amqpProtocolServer.incrementTotalRequests();
    }

    /**
     * Events handler for incoming messages
     *
     * @param messageReceived message received
     */
    @Override
    public void onMessageReceived(MessageReceived messageReceived) {
        String message = new String(messageReceived.getBody());
        String topic = messageReceived.getExchange();
        String host = messageReceived.getHost();
        int port = messageReceived.getPort();
        log.debug(String.format("Message received from %s:%d on topic %s with contents \"%s\"",
                host, port, topic, message
        ));

        String protocolServerType = amqpProtocolServer.getProtocolServerType();

        MessageService.getInstance().distributeMessage(new Message(message, topic, null, protocolServerType));
        amqpProtocolServer.incrementTotalMessagesReceived();
        amqpProtocolServer.incrementTotalRequests();
    }

    /**
     * Event handler for subscribe events
     *
     * @param subscribeMessage subscribe message
     */
    @Override
    public void onSubscribe(SubscribeMessage subscribeMessage) {
        log.debug(String.format("%s:%d subscribed on topic %s",
                subscribeMessage.getHost(), subscribeMessage.getPort(), subscribeMessage.getExchange()
        ));
        String host = subscribeMessage.getHost();
        int port = subscribeMessage.getPort();
        String topic = subscribeMessage.getExchange();
        Subscriber subscriber = new Subscriber(
                host, port, topic, amqpProtocolServer.getProtocolServerType()
        );
        subscriberMap.putSubscriber(subscriber);
        subscriptionService.addSubscriber(subscriber);
        amqpProtocolServer.incrementTotalRequests();
    }

    /**
     * Event handler for unsubscribe events
     *
     * @param unsubscribeMessage unsubscribe message
     */
    @Override
    public void onUnsubscribe(UnsubscribeMessage unsubscribeMessage) {
        log.debug(String.format("%s:%d unsubscribed on topic %s",
                unsubscribeMessage.getHost(), unsubscribeMessage.getPort(), unsubscribeMessage.getExchange()
        ));
        String host = unsubscribeMessage.getHost();
        int port = unsubscribeMessage.getPort();
        String topic = unsubscribeMessage.getExchange();

        Subscriber subscriber = subscriberMap.getSubscriber(host, port, topic);
        subscriptionService.removeSubscriber(subscriber);
        amqpProtocolServer.incrementTotalRequests();
    }

    /**
     * Setter injection of subscriber map. Used for tests
     *
     * @param subscriberMap subscriber map
     */
    public void setSubscriberMap(SubscriberMap subscriberMap) {
        this.subscriberMap = subscriberMap;
    }

    /**
     * Setter injection of subscription service. Used for tests
     *
     * @param subscriptionService subscription service
     */
    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
}
