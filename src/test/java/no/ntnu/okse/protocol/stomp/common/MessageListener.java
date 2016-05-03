package no.ntnu.okse.protocol.stomp.common;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;

public class MessageListener implements StampyMessageListener {

    @Override
    public boolean isForMessage(StampyMessage<?> message) {
        return true;
    }

    @Override
    public StompMessageType[] getMessageTypes() {
        return StompMessageType.values();
    }

    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {
    }

}
