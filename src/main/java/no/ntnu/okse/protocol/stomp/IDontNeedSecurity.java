package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.SecurityMessageListener;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class IDontNeedSecurity implements StampyMessageListener, SecurityMessageListener {
    @Override
    public StompMessageType[] getMessageTypes() {
        return null;
//        return new StompMessageType[0];
    }

    @Override
    public boolean isForMessage(StampyMessage<?> stampyMessage) {
        return false;
    }

    @Override
    public void messageReceived(StampyMessage<?> stampyMessage, HostPort hostPort) throws Exception {

    }
}
