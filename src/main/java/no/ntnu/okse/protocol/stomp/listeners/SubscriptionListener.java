package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.protocol.stomp.STOMPSubscriptionManager;

public class SubscriptionListener implements StampyMessageListener {
    private static StompMessageType[] TYPES;
    private STOMPSubscriptionManager subscriptionManager;
    private String protocol;
    public SubscriptionListener(){
        this.protocol = "stomp";
    }

    @Override
    public StompMessageType[] getMessageTypes() {
        return TYPES;
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
    }

    static {
        TYPES = new StompMessageType[]{StompMessageType.SUBSCRIBE};
    }

    public void setSubscriptionManager(STOMPSubscriptionManager subscriptionManager){
        this.subscriptionManager = subscriptionManager;
    }
}
