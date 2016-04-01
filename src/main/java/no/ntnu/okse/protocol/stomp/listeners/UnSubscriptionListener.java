package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class UnSubscriptionListener implements StampyMessageListener {
    private static StompMessageType[] TYPES;

    @Override
    public StompMessageType[] getMessageTypes() {
        return new StompMessageType[0];
    }

    @Override
    public boolean isForMessage(StampyMessage<?> stampyMessage) {
        return false;
    }

    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {

    }

    static {
        TYPES = new StompMessageType[]{StompMessageType.UNSUBSCRIBE};
    }
}
