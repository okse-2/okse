package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.topic.TopicService;
import no.ntnu.okse.protocol.stomp.STOMPSubscriptionManager;

/**
 * This class listens to the SUBSCRIBE message type and
 * handles any connection that wants to subscribe to a topic
 */
public class SubscriptionListener implements StampyMessageListener {
    private STOMPSubscriptionManager subscriptionManager;
    private String protocol;

    /**
     * Constructor for the class
     * Sets the protcol type to stomp
     */
    public SubscriptionListener(){
        this.protocol = "stomp";
    }

    @Override
    public StompMessageType[] getMessageTypes() {
        return new StompMessageType[]{StompMessageType.SUBSCRIBE};
    }

    @Override
    public boolean isForMessage(StampyMessage<?> stampyMessage) {
        return true;
    }

    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) {
        SubscribeMessage subMessage = (SubscribeMessage) stampyMessage;
        Subscriber sub = new Subscriber(hostPort.getHost(), hostPort.getPort(), subMessage.getHeader().getDestination(), protocol);
        subscriptionManager.addSubscriber(sub, subMessage.getHeader().getId());
        TopicService.getInstance().addTopic(sub.getTopic());
    }

    /**
     * Sets the subscriptionManager for this class, it is used
     * to remove any subscribers after they disconnect
     * @param subscriptionManager the subscription manager instance
     */
    public void setSubscriptionManager(STOMPSubscriptionManager subscriptionManager){
        this.subscriptionManager = subscriptionManager;
    }
}
