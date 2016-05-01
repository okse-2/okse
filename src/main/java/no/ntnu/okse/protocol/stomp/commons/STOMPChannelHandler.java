package no.ntnu.okse.protocol.stomp.commons;

import asia.stampy.common.gateway.*;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.netty.StampyNettyChannelHandler;
import asia.stampy.common.parsing.StompMessageParser;
import asia.stampy.common.parsing.UnparseableException;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Ogdans3 on 30.04.2016.
 */
public class STOMPChannelHandler extends StampyNettyChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private StompMessageParser parser = new StompMessageParser();

    private StampyHeartbeatContainer heartbeatContainer;

    private AbstractStampyMessageGateway gateway;

    private static final String ILLEGAL_ACCESS_ATTEMPT = "Illegal access attempt";

    private Executor executor = Executors.newSingleThreadExecutor();

    private UnparseableMessageHandler unparseableMessageHandler = new DefaultUnparseableMessageHandler();

    private Map<HostPort, Channel> sessions = new ConcurrentHashMap<HostPort, Channel>();

//    private StampyHandlerHelper helper = new StampyHandlerHelper();
    private STOMPStampyHandlerHelper helper = new STOMPStampyHandlerHelper();
    private STOMPProtocolServer protocolServer;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.netty.channel.SimpleChannelUpstreamHandler#messageReceived(org
     * .jboss.netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.MessageEvent)
     */
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        final HostPort hostPort = createHostPort(ctx);
        log.debug("Received raw message {} from {}", e.getMessage(), hostPort);

        helper.resetHeartbeat(hostPort);

        if (!helper.isValidObject(e.getMessage())) {
            log.error("Object {} is not a valid STOMP message, closing connection {}", e.getMessage(), hostPort);
            illegalAccess(ctx);
            return;
        }

        final String msg = (String) e.getMessage();

        if (helper.isHeartbeat(msg)) {
            log.trace("Received heartbeat");
            return;
        }

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                asyncProcessing(hostPort, msg);
            }
        };

        getExecutor().execute(runnable);
    }

    /**
     * Creates the host port.
     *
     * @param ctx
     *          the ctx
     * @return the host port
     */
    protected HostPort createHostPort(ChannelHandlerContext ctx) {
        return new HostPort((InetSocketAddress) ctx.getChannel().getRemoteAddress());
    }

    /**
     * Invoked when a {@link Channel} is open, bound to a local address, and
     * connected to a remote address. <br/>
     *
     * <strong>Be aware that this event is fired from within the Boss-Thread so
     * you should not execute any heavy operation in there as it will block the
     * dispatching to other workers!</strong>
     *
     * @param ctx
     *          the ctx
     * @param e
     *          the e
     * @throws Exception
     *           the exception
     */
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        HostPort hostPort = createHostPort(ctx);
        sessions.put(hostPort, ctx.getChannel());
        ctx.sendUpstream(e);
    }

    /**
     * Invoked when a {@link Channel} was disconnected from its remote peer.
     *
     * @param ctx
     *          the ctx
     * @param e
     *          the e
     * @throws Exception
     *           the exception
     */
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        HostPort hostPort = createHostPort(ctx);
        sessions.remove(hostPort);
        ctx.sendUpstream(e);
    }

    /**
     * Gets the connected host ports.
     *
     * @return the connected host ports
     */
    public Set<HostPort> getConnectedHostPorts() {
        return Collections.unmodifiableSet(sessions.keySet());
    }

    /**
     * Checks if is connected.
     *
     * @param hostPort
     *          the host port
     * @return true, if is connected
     */
    public boolean isConnected(HostPort hostPort) {
        return sessions.containsKey(hostPort);
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

    /**
     * Close.
     *
     * @param hostPort
     *          the host port
     */
    public void close(HostPort hostPort) {
        if (!isConnected(hostPort)) {
            log.warn("{} is already closed");
            return;
        }

        Channel channel = sessions.get(hostPort);
        ChannelFuture cf = channel.close();
        cf.awaitUninterruptibly();
        log.info("Session for {} has been closed", hostPort);
    }

    /**
     * Once simple validation has been performed on the received message a
     * Runnable is executed by a single thread executor. This pulls the messages
     * off the thread NETTY uses and ensures the messages are processed in the
     * order they are received.
     *
     * @param hostPort
     *          the host port
     * @param msg
     *          the msg
     */
    protected void asyncProcessing(HostPort hostPort, String msg) {
        StampyMessage<?> sm = null;
        try {
            sm = getParser().parseMessage(msg);

            getGateway().notifyMessageListeners(sm, hostPort);
        } catch (UnparseableException e) {
            helper.handleUnparseableMessage(hostPort, msg, e);
        } catch (MessageListenerHaltException e) {
            // halting
        } catch (Exception e) {
            helper.handleUnexpectedError(hostPort, msg, sm, e);
        }
    }

    /**
     * Illegal access.
     *
     * @param ctx
     *          the ctx
     */
    protected void illegalAccess(ChannelHandlerContext ctx) {
        ChannelFuture cf = ctx.getChannel().write(ILLEGAL_ACCESS_ATTEMPT);
        cf.awaitUninterruptibly();
        cf = ctx.getChannel().close();
        cf.awaitUninterruptibly();
    }

    /**
     * Gets the parser.
     *
     * @return the parser
     */
    public StompMessageParser getParser() {
        return parser;
    }

    /**
     * Sets the parser.
     *
     * @param parser
     *          the new parser
     */
    public void setParser(StompMessageParser parser) {
        this.parser = parser;
        helper.setParser(parser);
    }

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
     * Gets the gateway.
     *
     * @return the gateway
     */
    public AbstractStampyMessageGateway getGateway() {
        return gateway;
    }

    /**
     * Sets the gateway.
     *
     * @param gateway
     *          the new gateway
     */
    public void setGateway(AbstractStampyMessageGateway gateway) {
        this.gateway = gateway;
        helper.setGateway(gateway);
    }

    /**
     * Gets the unparseable message handler.
     *
     * @return the unparseable message handler
     */
    public UnparseableMessageHandler getUnparseableMessageHandler() {
        return unparseableMessageHandler;
    }

    /**
     * Sets the unparseable message handler.
     *
     * @param unparseableMessageHandler
     *          the new unparseable message handler
     */
    public void setUnparseableMessageHandler(UnparseableMessageHandler unparseableMessageHandler) {
        this.unparseableMessageHandler = unparseableMessageHandler;
        helper.setUnparseableMessageHandler(unparseableMessageHandler);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org
     * .jboss.netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ExceptionEvent)
     */
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        HostPort hostPort = createHostPort(ctx);
        log.error("Unexpected Netty exception for " + hostPort, e.getCause());
    }

    /**
     * Gets the executor.
     *
     * @return the executor
     */
    public Executor getExecutor() {
        return executor;
    }

    /**
     * Sets the executor.
     *
     * @param executor
     *          the new executor
     */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }


    public void setProtocolServer(STOMPProtocolServer protocolServer) {
        this.protocolServer = protocolServer;
    }
}
