package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.netty.ServerNettyMessageGateway;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.stomp.SubscriptionManager;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class SubscriptionListener implements StampyMessageListener {
    private static StompMessageType[] TYPES;
    private SubscriptionManager subscriptionManager;
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
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {
        System.out.println("Subscriber message: host and port, " + hostPort.getHost() + " : " + hostPort.getPort() + "\n");
        SubscribeMessage subMessage = (SubscribeMessage)stampyMessage;
        Subscriber sub = new Subscriber(hostPort.getHost(), hostPort.getPort(), subMessage.getHeader().getDestination(), protocol);
        subscriptionManager.addSubscriber(sub, subMessage.getHeader().getId());
    }

    static {
        TYPES = new StompMessageType[]{StompMessageType.SUBSCRIBE};
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager){
        this.subscriptionManager = subscriptionManager;
    }
}
