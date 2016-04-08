package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.SecurityMessageListener;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;

public class IDontNeedSecurity implements StampyMessageListener, SecurityMessageListener {
    @Override
    public StompMessageType[] getMessageTypes() {
        return null;
    }

    @Override
    public boolean isForMessage(StampyMessage<?> stampyMessage) {
        return false;
    }

    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {

    }
}
