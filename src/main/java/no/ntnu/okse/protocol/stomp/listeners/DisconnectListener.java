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

public class DisconnectListener implements StampyMessageListener{
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private STOMPSubscriptionManager subscriptionManager;
    private ServerNettyMessageGateway gateway;

    protected void ensureCleanup() {
        gateway.addHandler(new SimpleChannelUpstreamHandler() {
            public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                log.info("Cleaning up after a forceful disconnect");
                HostPort hostPort = new HostPort((InetSocketAddress) ctx.getChannel().getRemoteAddress());
                cleanUp(hostPort);
            }
        });
    }

    public void cleanUp(HostPort hostPort){
        subscriptionManager.removeSubscriber(hostPort.getHost(), hostPort.getPort());
    }

    public void setSubscriptionManager(STOMPSubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

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
