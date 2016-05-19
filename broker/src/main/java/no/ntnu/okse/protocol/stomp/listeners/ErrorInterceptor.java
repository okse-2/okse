package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.common.gateway.ConnectError;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;

/**
 * Intercepts errors that are given from the Netty gateway, this is done so that we can count
 * total errors
 */
public class ErrorInterceptor implements asia.stampy.common.gateway.ErrorInterceptor {
    private STOMPProtocolServer ps;

    @Override
    public void onError(ConnectError connectError) {
        ps.incrementTotalErrors();
    }

    /**
     * Sets the protocol server
     * @param ps the protocol server instance
     */
    public void setProtocolServer(STOMPProtocolServer ps){
        this.ps = ps;
    }
}
