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
import no.ntnu.okse.protocol.mqtt.MQTTProtocolServer;
import no.ntnu.okse.protocol.stomp.listeners.*;
import no.ntnu.okse.protocol.stomp.listeners.MessageListener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by ogdans3 on 3/30/16.
 */
public class STOMPServer extends Server {
    private static SubscriptionManager subscriptionManager;
    private static AbstractStampyMessageGateway gateway;

    public AbstractStampyMessageGateway initialize() {
        HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

        ServerNettyMessageGateway gateway = new ServerNettyMessageGateway();
        gateway.setPort(5551);
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

        gateway.addOutgoingMessageInterceptor(new OutgoingListener());


        return gateway;
    }

    private void addGatewayListeners(ServerNettyMessageGateway gateway){
        no.ntnu.okse.protocol.stomp.listeners.MessageListener messageListener = new MessageListener();
        SubscriptionListener subListener = new SubscriptionListener();

        messageListener.setSubscriptionManager(subscriptionManager);
        subListener.setSubscriptionManager(subscriptionManager);

        messageListener.setGateway(gateway);

        gateway.addMessageListener(messageListener);
        gateway.addMessageListener(new ConnectListener());
        gateway.addMessageListener(subListener);
        gateway.addMessageListener(new UnSubscriptionListener());
        gateway.addMessageListener(new DisconnectListener());
    }

    public void init() throws Exception {
        subscriptionManager = new SubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());


        gateway = initialize();
        gateway.connect();
        System.out.println("STOMP server started correctly, port: " + gateway.getPort());

        ClientTest publisherClient = new ClientTest();
        ClientTest subscriberClient = new ClientTest();
        publisherClient.init();
        publisherClient.testConnect("publisher");

        subscriberClient.init();
        subscriberClient.testConnect("subscriber");
        subscriberClient.testSubscription();

        Thread.sleep(1000);
        System.out.println("\n\n");
        publisherClient.testMessage();


        while(true){
            System.out.println("Send message");
            publisherClient.testMessage();
            Thread.sleep(3000);
        }

    }

    public static void main(String[] args) {
        STOMPServer test = new STOMPServer();
        try {
            test.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the message to any subscriber that is subscribed to the topic that the message was sent to
     * @param message is the message that is sent from OKSE core
     * */
    public void sendMessage(@NotNull Message message) throws InterceptException {
        System.out.println("OKSE has received a message, please redistribute!");

        MessageMessage msg = createSTOMPMessage(message);

        HashSet<Subscriber> subs = subscriptionManager.getAllSubscribersForTopic(message.getTopic());
        Map<String, Subscriber> map = (Map<String, Subscriber>) subs;
        Set<String> keyset = map.keySet();
        Iterator it = keyset.iterator();

        while(it.hasNext()){
            String key = (String) it.next();
            Subscriber sub = map.get(key);

            //Its more correct to recreate the message, however we only have to change the subscription id. So we simply replace it!
            //TODO: Do we also have to change the message id?
            msg.getHeader().setSubscription(key);
            gateway.sendMessage((StampyMessage<?>) message, new HostPort(sub.getHost(), sub.getPort()));

            //TODO: Need to increment the number of messages sent
        }
    }

    private MessageMessage createSTOMPMessage(Message msg){
        String msgId = msg.getMessageID();
        MessageMessage message = new MessageMessage(msg.getTopic(), msgId, "MUST_BE_REPLACED");

        message.setBody(msg.getMessage());
        message.getHeader().setAck(msgId);
        return message;
    }
}
