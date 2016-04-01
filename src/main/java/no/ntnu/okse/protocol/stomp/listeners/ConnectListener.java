package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.protocol.stomp.SubscriptionManager;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class ConnectListener implements StampyMessageListener {
    private static final StompMessageType[] TYPES;

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
        System.out.println("Connect message: " + hostPort.getHost() + " : " + hostPort.getPort());
    }

    static {
        TYPES = new StompMessageType[]{StompMessageType.CONNECT};
    }

}
