package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;

/**
 * Created by Ogdans3 on 03.04.2016.
 */
public class IncrementTotalRequestsListener implements StampyMessageListener {
    private STOMPProtocolServer protocolServer;

    public void setProtocolServer(STOMPProtocolServer protocolServer) {
        this.protocolServer = protocolServer;
    }

    @Override
    public StompMessageType[] getMessageTypes() {
        return StompMessageType.values();
    }

    @Override
    public boolean isForMessage(StampyMessage<?> message) {
        return true;
    }

    @Override
    public void messageReceived(StampyMessage<?> message, HostPort hostPort) throws Exception {
        protocolServer.incrementTotalRequests();
    }
}
