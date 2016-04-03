package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.server.listener.validate.ServerMessageValidationListener;
import asia.stampy.server.listener.version.VersionListener;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.netty.ServerNettyChannelHandler;
import asia.stampy.server.netty.ServerNettyMessageGateway;
import asia.stampy.server.netty.connect.NettyConnectResponseListener;
import asia.stampy.server.netty.connect.NettyConnectStateListener;
import asia.stampy.server.netty.heartbeat.NettyHeartbeatListener;
import asia.stampy.server.netty.login.NettyLoginMessageListener;
import asia.stampy.server.netty.receipt.NettyReceiptListener;
import asia.stampy.server.netty.subscription.NettyAcknowledgementListenerAndInterceptor;
import asia.stampy.server.netty.transaction.NettyTransactionListener;
import com.sun.istack.internal.NotNull;
import io.moquette.server.Server;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.stomp.listeners.*;
import no.ntnu.okse.protocol.stomp.listeners.MessageListener;

import java.util.*;

/**
 * Created by ogdans3 on 3/30/16.
 */
public class STOMPServer extends Server {
    private static SubscriptionManager subscriptionManager;
    private static AbstractStampyMessageGateway gateway;
    private static STOMPProtocolServer ps;

    public void setSubscriptionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    public AbstractStampyMessageGateway initialize() {
        HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

        ServerNettyMessageGateway gateway = new ServerNettyMessageGateway();
        gateway.setHeartbeat(1000);
        gateway.setAutoShutdown(true);

        ServerNettyChannelHandler channelHandler = new ServerNettyChannelHandler();
        channelHandler.setGateway(gateway);
        channelHandler.setHeartbeatContainer(heartbeatContainer);

        gateway.addMessageListener(new IDontNeedSecurity()); // DON'T DO THIS!!!

        gateway.addMessageListener(new ServerMessageValidationListener());

        gateway.addMessageListener(new VersionListener());


        NettyLoginMessageListener login = new NettyLoginMessageListener();
        login.setGateway(gateway);
        login.setLoginHandler(new SystemLoginHandler());
        gateway.addMessageListener(login);

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
        acknowledgement.setHandler(sys);
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

        gateway.setHandler(channelHandler);

        addGatewayListeners(gateway);

        return gateway;
    }

    private void addGatewayListeners(ServerNettyMessageGateway gateway){
        no.ntnu.okse.protocol.stomp.listeners.MessageListener messageListener = new MessageListener();
        SubscriptionListener subListener = new SubscriptionListener();
        UnSubscriptionListener unsubListener = new UnSubscriptionListener();
        ConnectListener connectListener = new ConnectListener();
        DisconnectListener disconnectListener = new DisconnectListener();
        IncrementTotalRequestsListener incrementTotalRequestsListener = new IncrementTotalRequestsListener();

        messageListener.setSubscriptionManager(subscriptionManager);
        subListener.setSubscriptionManager(subscriptionManager);
        unsubListener.setSubscriptionManager(subscriptionManager);
        disconnectListener.setSubscriptionManager(subscriptionManager);

        messageListener.setGateway(gateway);

        messageListener.setProtocolServer(ps);
        incrementTotalRequestsListener.setProtocolServer(ps);

        gateway.addMessageListener(connectListener);
        gateway.addMessageListener(subListener);
        gateway.addMessageListener(unsubListener);
        gateway.addMessageListener(disconnectListener);
        gateway.addMessageListener(messageListener);
    }

    public void init(STOMPProtocolServer stompProtocolServer, String host, int port) throws Exception {
        ps = stompProtocolServer;
        gateway = initialize();
        gateway.setPort(port);
        gateway.connect();
    }

    /**
     * Sends the message to any subscriber that is subscribed to the topic that the message was sent to
     * @param message is the message that is sent from OKSE core
     * */
    public void sendMessage(@NotNull Message message) throws InterceptException {
        System.out.println("OKSE has received a message, please redistribute!");

        HashMap<String, Subscriber> subs = subscriptionManager.getAllSubscribersForTopic(message.getTopic());

        Object[] keys = subs.keySet().toArray();
        for(int i = 0; i < subs.size(); i++){
            String key = (String)keys[i];
            Subscriber sub = subs.get(key);

            //TODO: Do we also have to change the message id?
            MessageMessage msg = createSTOMPMessage(message, key);
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
}
