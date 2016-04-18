package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.examples.system.server.SystemAcknowledgementHandler;
import asia.stampy.examples.system.server.SystemLoginHandler;
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
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.stomp.common.Gateway;
import no.ntnu.okse.protocol.stomp.listeners.IDontNeedSecurity;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.*;

public class STOMPServerTest {

    private STOMPServer server_spy;
    private STOMPProtocolServer ps_spy;
    private AbstractStampyMessageGateway gateway;
    private STOMPSubscriptionManager subManager_spy;

    public void setUp(int port) throws Exception {
        STOMPServer server = new STOMPServer();
        STOMPProtocolServer ps = new STOMPProtocolServer("localhost", port);
        STOMPSubscriptionManager subManager = new STOMPSubscriptionManager();

        subManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        subManager_spy = Mockito.spy(subManager);
        server.setSubscriptionManager(subManager_spy);

        server_spy = Mockito.spy(server);
        ps_spy = Mockito.spy(ps);
    }

    private void startGateway(int port) throws Exception {
        server_spy.setProtocolServer(ps_spy);
        gateway = Gateway.initialize(port);
        gateway.connect();
        gateway.shutdown();
        server_spy.gateway = gateway;
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if(gateway != null)
            gateway.shutdown();
        server_spy = null;
        ps_spy = null;
        gateway = null;
    }

    @Test
    public void sendMessage() throws Exception {
        setUp(61653);
        startGateway(61653);
        subManager_spy.addSubscriber(new Subscriber("localhost", 61613, "testing", "stomp"), "ogdans3");

        ArgumentCaptor<StampyMessage> messageArgument = ArgumentCaptor.forClass(StampyMessage.class);
        ArgumentCaptor<HostPort> hostPortArgument = ArgumentCaptor.forClass(HostPort.class);

        Message msg = new Message("testing", "testing", null, "stomp");
        msg.setAttribute("test", "user defined attribute");
        server_spy.sendMessage(msg);
        Mockito.verify(gateway).sendMessage(messageArgument.capture(), hostPortArgument.capture());
    }

    @Test
    public void init() throws Exception {
        STOMPServer server = new STOMPServer();
        server_spy = Mockito.spy(server);
        int port = 61634;
        server_spy.init("localhost", port);
        gateway = server_spy.gateway;
        assertEquals(port, server_spy.gateway.getPort());
        assertNotNull(null, server_spy.gateway);
    }

    @Test
    public void stopServer() throws Exception {
        //TODO: Implement this after stopServer is implemented
    }

    @Test
    public void stopServerCatchException() throws Exception {
        //TODO: Implement this after stopServer is implemented
    }
}
