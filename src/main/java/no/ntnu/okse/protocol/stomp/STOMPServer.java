package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.server.listener.validate.ServerMessageValidationListener;
import asia.stampy.server.listener.version.VersionListener;
import asia.stampy.server.netty.ServerNettyChannelHandler;
import asia.stampy.server.netty.ServerNettyMessageGateway;
import asia.stampy.server.netty.connect.NettyConnectResponseListener;
import asia.stampy.server.netty.connect.NettyConnectStateListener;
import asia.stampy.server.netty.heartbeat.NettyHeartbeatListener;
import asia.stampy.server.netty.login.NettyLoginMessageListener;
import asia.stampy.server.netty.receipt.NettyReceiptListener;
import asia.stampy.server.netty.subscription.NettyAcknowledgementListenerAndInterceptor;
import asia.stampy.server.netty.transaction.NettyTransactionListener;
import net.ser1.stomp.*;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.mqtt.MQTTSubscriptionManager;
import no.ntnu.okse.protocol.stomp.listeners.*;
import no.ntnu.okse.protocol.stomp.listeners.MessageListener;

import java.io.IOException;
import java.util.Map;

/**
 * Created by ogdans3 on 3/30/16.
 */
public class STOMPServer {
    static Server server;
    static Stomp client;
    private static SubscriptionManager subscriptionManager;


    public static AbstractStampyMessageGateway initialize() {
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

        no.ntnu.okse.protocol.stomp.listeners.MessageListener temp = new MessageListener();
        temp.setSubscriptionManager(subscriptionManager);
        temp.setGateway(gateway);
        gateway.addMessageListener(temp);

        SubscriptionListener subListener = new SubscriptionListener();
        subListener.setSubscriptionManager(subscriptionManager);
        gateway.addMessageListener(subListener);


        gateway.addMessageListener(new ConnectListener());
        gateway.addMessageListener(new UnSubscriptionListener());
        gateway.addMessageListener(new DisconnectListener());

        gateway.addOutgoingMessageInterceptor(new OutgoingListener());


        return gateway;
    }

    public void init() throws Exception {
        subscriptionManager = new SubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());


        AbstractStampyMessageGateway gateway = initialize();
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

/*
        while(true){
            System.out.println("Send message");
            publisherClient.testMessage();
            Thread.sleep(1000);
        }
*/
    }

    public static void main(String[] args) throws Exception {
        STOMPServer test = new STOMPServer();
        test.init();
    }
}
