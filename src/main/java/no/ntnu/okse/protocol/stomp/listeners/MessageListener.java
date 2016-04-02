package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.server.netty.ServerNettyMessageGateway;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.protocol.stomp.SubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class MessageListener implements StampyMessageListener {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static StompMessageType[] TYPES;
    private ServerNettyMessageGateway gateway;
    private SubscriptionManager subscriptionManager;

    static {
        TYPES = new StompMessageType[]{StompMessageType.SEND};
    }

    private static final String VERSION = "1.2";

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
        System.out.println("Server: Message type: " + stampyMessage.getMessageType().toString());
        System.out.println(stampyMessage.toString() + "\n");
        sendMessage(stampyMessage, hostPort);
    }

    private void sendMessage(StampyMessage<?> stampyMessage, HostPort hostPort) throws InterceptException {
        Subscriber sub = subscriptionManager.getSubscriber("Test");
//        gateway.sendMessage("Test", new HostPort(sub.getHost(), sub.getPort()));
        gateway.broadcastMessage(stampyMessage);
//        gateway.broadcastMessage("Fuckers");
        System.out.println("Meesage sent to: " + sub.getHost() + " : " + sub.getPort());
    }

    public void setGateway(ServerNettyMessageGateway gateway){
        this.gateway = gateway;
    }
    public void setSubscriptionManager(SubscriptionManager subscriptionManager){
        this.subscriptionManager = subscriptionManager;
    }
}
