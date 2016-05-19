package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;

/**
 * This class listens to any message types
 * for each message that is received we want to increase the
 * total number of requests, that is the sole responsibility
 * for this class
 */
public class IncrementTotalRequestsListener implements StampyMessageListener {
    private STOMPProtocolServer protocolServer;

    /**
     * Sets the protocol server, used for incrementing the total
     * number of requests
     * @param protocolServer the protocol server
     */
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
