package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.message.interceptor.StampyOutgoingMessageInterceptor;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;

/**
 * Created by Ogdans3 on 30.04.2016.
 */
public class ErrorListener implements StampyOutgoingMessageInterceptor {
    private STOMPProtocolServer protocolServer;

    @Override
    public StompMessageType[] getMessageTypes() {
        return new StompMessageType[]{StompMessageType.ERROR};
    }

    @Override
    public boolean isForMessage(StampyMessage<?> message) {
        return true;
    }

    @Override
    public void interceptMessage(StampyMessage<?> message) throws InterceptException {
        protocolServer.incrementTotalBadRequests();
    }

    @Override
    public void interceptMessage(StampyMessage<?> message, HostPort hostPort) throws InterceptException {
        protocolServer.incrementTotalBadRequests();
    }

    public void setProtocolServer(STOMPProtocolServer protocolServer) {
        this.protocolServer = protocolServer;
    }
}
