package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.server.netty.ServerNettyMessageGateway;
import no.ntnu.okse.protocol.stomp.STOMPSubscriptionManager;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;

/**
 * This class listens for any DISCONNECT message type that are sent to the
 * STOMP server.
 *
 * IT also adds a handler to the gateway that cleans up
 * lingering subscribers if there is a forcefull disconnect without
 * there being sent a disonnect message
 */
public class DisconnectListener implements StampyMessageListener{
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private STOMPSubscriptionManager subscriptionManager;
    private ServerNettyMessageGateway gateway;

    /**
     * Cleans up any lingering subscribers if a connection
     * is terminated without a disconnect message
     */
    protected void ensureCleanup() {
        gateway.addHandler(new SimpleChannelUpstreamHandler() {
            public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                log.info("Cleaning up after a forceful disconnect");
                HostPort hostPort = new HostPort((InetSocketAddress) ctx.getChannel().getRemoteAddress());
                cleanUp(hostPort);
            }
        });
    }

    /**
     * Actually the method that removes any lingering subscribers
     * @param hostPort the host and port of the subscriber
     */
    public void cleanUp(HostPort hostPort){
        subscriptionManager.removeSubscriber(hostPort.getHost(), hostPort.getPort());
    }

    /**
     * Sets the subscriptionManager for this class, it is used
     * to remove any subscribers after they disconnect
     * @param subscriptionManager the subscription manager instance
     */
    public void setSubscriptionManager(STOMPSubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    /**
     * Sets the gateway
     * Used to add the handler for forcefull disconnects
     * @param gateway
     */
    public void setGateway(ServerNettyMessageGateway gateway) {
        this.gateway = gateway;
        ensureCleanup();
    }

    @Override
    public StompMessageType[] getMessageTypes() {
        return new StompMessageType[]{StompMessageType.DISCONNECT};
    }

    @Override
    public boolean isForMessage(StampyMessage<?> message) {
        return true;
    }
    
    @Override
    public void messageReceived(StampyMessage<?> message, HostPort hostPort) throws Exception {
        log.info("Cleaning up after a disconnect");
        cleanUp(hostPort);
    }
}
