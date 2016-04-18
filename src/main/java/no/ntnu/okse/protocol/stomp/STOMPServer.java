package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.examples.system.server.SystemAcknowledgementHandler;
import asia.stampy.server.listener.validate.ServerMessageValidationListener;
import asia.stampy.server.listener.version.VersionListener;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.netty.ServerNettyChannelHandler;
import asia.stampy.server.netty.ServerNettyMessageGateway;
import asia.stampy.server.netty.connect.NettyConnectResponseListener;
import asia.stampy.server.netty.connect.NettyConnectStateListener;
import asia.stampy.server.netty.heartbeat.NettyHeartbeatListener;
import asia.stampy.server.netty.receipt.NettyReceiptListener;
import asia.stampy.server.netty.subscription.NettyAcknowledgementListenerAndInterceptor;
import asia.stampy.server.netty.transaction.NettyTransactionListener;
import io.moquette.server.Server;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.protocol.stomp.listeners.*;
import no.ntnu.okse.protocol.stomp.listeners.MessageListener;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import javax.validation.constraints.NotNull;
import java.util.*;

public class STOMPServer extends Server {
    private static STOMPSubscriptionManager subscriptionManager;
    public AbstractStampyMessageGateway gateway;
    private static STOMPProtocolServer ps;
    private Logger log;

    public STOMPServer(){
        log = Logger.getLogger(STOMPProtocolServer.class.getName());
    }

    public void setSubscriptionManager(STOMPSubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    private AbstractStampyMessageGateway initialize(int port) {
        StampyHeartbeatContainer heartbeatContainer = new HeartbeatContainer();

        ServerNettyMessageGateway gateway = new ServerNettyMessageGateway();
        gateway.setPort(port);
        gateway.setHeartbeat(1000);
        gateway.setAutoShutdown(true);

        ServerNettyChannelHandler channelHandler = new ServerNettyChannelHandler();
        channelHandler.setGateway(gateway);
        channelHandler.setHeartbeatContainer(heartbeatContainer);

        gateway.addMessageListener(new IDontNeedSecurity());

        gateway.addMessageListener(new ServerMessageValidationListener());

        gateway.addMessageListener(new VersionListener());

        //It seems that this listener needs to be placed here for some odd reason
        //TODO: Investigate further, want to move this to the addGatewayListenersAndHandlers method
        DisconnectListener disconnectListener = new DisconnectListener();

        disconnectListener.setSubscriptionManager(subscriptionManager);
        disconnectListener.setGateway(gateway);
        gateway.addMessageListener(disconnectListener);


        NettyConnectStateListener connect = new NettyConnectStateListener();
        connect.setGateway(gateway);
        gateway.addMessageListener(connect);

        NettyHeartbeatListener heartbeat = new NettyHeartbeatListener();
        heartbeat.setHeartbeatContainer(heartbeatContainer);
        heartbeat.setGateway(gateway);
        gateway.addMessageListener(heartbeat);

        NettyTransactionListener transaction = new NettyTransactionListener();
        transaction.setGateway(gateway);
        gateway.addMessageListener(transaction);

        SystemAcknowledgementHandler sys = new SystemAcknowledgementHandler();

        NettyAcknowledgementListenerAndInterceptor acknowledgement = new NettyAcknowledgementListenerAndInterceptor();
        acknowledgement.setGateway(gateway);
        acknowledgement.setAckTimeoutMillis(200);
        gateway.addMessageListener(acknowledgement);
        gateway.addOutgoingMessageInterceptor(acknowledgement);

        NettyReceiptListener receipt = new NettyReceiptListener();
        receipt.setGateway(gateway);
        gateway.addMessageListener(receipt);

        NettyConnectResponseListener connectResponse = new NettyConnectResponseListener();
        connectResponse.setGateway(gateway);
        gateway.addMessageListener(connectResponse);

        addGatewayListenersAndHandlers(gateway);
        acknowledgement.setHandler(sys);
        gateway.setHandler(channelHandler);

        return gateway;
    }

    private void addGatewayListenersAndHandlers(ServerNettyMessageGateway gateway){
        no.ntnu.okse.protocol.stomp.listeners.MessageListener messageListener = new MessageListener();
        SubscriptionListener subListener = new SubscriptionListener();
        UnSubscriptionListener unsubListener = new UnSubscriptionListener();
        IncrementTotalRequestsListener incrementTotalRequestsListener = new IncrementTotalRequestsListener();

        subListener.setSubscriptionManager(subscriptionManager);
        unsubListener.setSubscriptionManager(subscriptionManager);

        messageListener.setProtocolServer(ps);
        incrementTotalRequestsListener.setProtocolServer(ps);

        messageListener.setMessageService(MessageService.getInstance());

        gateway.addMessageListener(subListener);
        gateway.addMessageListener(unsubListener);
        gateway.addMessageListener(messageListener);
        gateway.addMessageListener(incrementTotalRequestsListener);
    }

    public void init(String host, int port) throws Exception {
        gateway = initialize(port);
        gateway.connect();
    }

    public void setProtocolServer(STOMPProtocolServer ps){
        this.ps = ps;
    }

    /**
     * Sends the message to any subscriber that is subscribed to the topic that the message was sent to
     * @param message is the message that is sent from OKSE core
     * */
    public void sendMessage(@NotNull Message message) throws InterceptException {
        log.debug("OKSE has received a message, please redistribute!");

        HashMap<String, Subscriber> subs = subscriptionManager.getAllSubscribersForTopic(message.getTopic());

        Object[] keys = subs.keySet().toArray();
        for(int i = 0; i < subs.size(); i++){
            String key = (String)keys[i];
            Subscriber sub = subs.get(key);

            //TODO: Do we also have to change the message id?
            MessageMessage msg = createSTOMPMessage(message, key);
            System.out.println("Send message");
            gateway.sendMessage((StampyMessage<?>) msg, new HostPort(sub.getHost(), sub.getPort()));
            ps.incrementTotalMessagesSent();
        }
    }

    private MessageMessage createSTOMPMessage(Message msg, String id){
        String msgId = msg.getMessageID();
        MessageMessage message = new MessageMessage(msg.getTopic(), msgId, id);
        
        message.setBody(msg.getMessage());
        message.getHeader().setAck(msgId);
        return message;
    }

    public void stopServer(){
        //TODO: This needs to be implemented, will be left like this because of demo purposes
/*        try {
            gateway.shutdown();
            gateway = null;
        } catch (Exception e) {
            log.error("Exception when trying to shutdown the server", e);
        }
*/
    }
}
