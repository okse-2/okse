package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.protocol.stomp.SubscriptionManager;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class SubscriptionListener implements StampyMessageListener {
    private static StompMessageType[] TYPES;
    private SubscriptionManager subscriptionManager;

    @Override
    public StompMessageType[] getMessageTypes() {
        return TYPES;
    }

    @Override
    public boolean isForMessage(StampyMessage<?> stampyMessage) {
        return true;
    }

    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {
        System.out.println("Subscriber message: host and port, " + hostPort.getHost() + " : " + hostPort.getPort() + "\n");
        subscriptionManager.addSubscriber(new Subscriber(hostPort.getHost(), hostPort.getPort(), "Test", "stomp"), "Test");
    }

    static {
        TYPES = new StompMessageType[]{StompMessageType.SUBSCRIBE};
    }
    public void setSubscriptionManager(SubscriptionManager subscriptionManager){
        this.subscriptionManager = subscriptionManager;
    }
}
