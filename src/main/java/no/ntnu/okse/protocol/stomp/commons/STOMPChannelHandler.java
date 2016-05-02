package no.ntnu.okse.protocol.stomp.commons;

import asia.stampy.common.gateway.*;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.common.netty.StampyNettyChannelHandler;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Ogdans3 on 30.04.2016.
 */
public class STOMPChannelHandler extends StampyNettyChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Map<HostPort, Channel> sessions = new ConcurrentHashMap<HostPort, Channel>();

    private STOMPStampyHandlerHelper helper = new STOMPStampyHandlerHelper();
    private STOMPProtocolServer protocolServer;
    private StampyHeartbeatContainer heartbeatContainer;

    /**
     * Gets the heartbeat container.
     *
     * @return the heartbeat container
     */
    public StampyHeartbeatContainer getHeartbeatContainer() {
        return heartbeatContainer;
    }

    /**
     * Sets the heartbeat container.
     *
     * @param heartbeatContainer
     *          the new heartbeat container
     */
    public void setHeartbeatContainer(StampyHeartbeatContainer heartbeatContainer) {
        this.heartbeatContainer = heartbeatContainer;
        helper.setHeartbeatContainer(heartbeatContainer);
    }

    /**
     * Broadcast message.
     *
     * @param message
     *          the message
     */
    public void broadcastMessage(String message) {
        for (Channel channel : sessions.values()) {
            sendMessage(message, null, channel);
        }
    }

    /**
     * Send message.
     *
     * @param message
     *          the message
     * @param hostPort
     *          the host port
     */
    public void sendMessage(String message, HostPort hostPort) {
        sendMessage(message, hostPort, sessions.get(hostPort));
    }

    private synchronized void sendMessage(String message, HostPort hostPort, Channel channel) {
        if (channel == null || !channel.isConnected()) {
            log.error("Channel is not connected, cannot send message {}", message);
            protocolServer.incrementTotalErrors();
            return;
        }

        if (hostPort == null) hostPort = new HostPort((InetSocketAddress) channel.getRemoteAddress());
        helper.resetHeartbeat(hostPort);

        channel.write(message);
    }

    public void setProtocolServer(STOMPProtocolServer protocolServer) {
        this.protocolServer = protocolServer;
    }

    public STOMPProtocolServer getProtocolServer() {
        return protocolServer;
    }

    public void setSessions(ConcurrentHashMap<HostPort, Channel> sessions){
        this.sessions = sessions;
    }
}
