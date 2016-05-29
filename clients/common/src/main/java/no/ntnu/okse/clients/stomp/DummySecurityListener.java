package no.ntnu.okse.clients.stomp;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.SecurityMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;

/**
 * Security listener that accepts everything
 */
public class DummySecurityListener implements SecurityMessageListener {
    @Override
    public StompMessageType[] getMessageTypes() {
        return null;
    }

    @Override
    public boolean isForMessage(StampyMessage<?> stampyMessage) {
        return false;
    }

    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {}
}
