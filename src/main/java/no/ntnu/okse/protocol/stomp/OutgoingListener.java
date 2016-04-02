package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor;
import asia.stampy.server.netty.ServerNettyMessageGateway;

/**
 * Created by ogdans3 on 4/1/16.
 */
public class OutgoingListener implements StampyOutgoingMessageInterceptor {
    private static StompMessageType[] TYPES;
    static {
        TYPES = StompMessageType.values();
    }

    @Override
    public StompMessageType[] getMessageTypes() {
        return TYPES;
    }

    @Override
    public boolean isForMessage(StampyMessage<?> stampyMessage) {
        return false;
    }

    @Override
    public void interceptMessage(StampyMessage<?> stampyMessage) throws InterceptException {
        System.out.println("Outgoing message: " + stampyMessage.toString() + "\n\n\n");
    }

    @Override
    public void interceptMessage(StampyMessage<?> stampyMessage, HostPort hostPort) throws InterceptException {
        System.out.println("Outgoing message: " + stampyMessage.toString() + "\n\n\n");
    }
}
