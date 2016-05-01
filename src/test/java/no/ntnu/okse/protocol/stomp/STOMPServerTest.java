package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StampyMessage;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.stomp.commons.STOMPGateway;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.*;

public class STOMPServerTest {

    private STOMPServer server_spy;
    private STOMPProtocolServer ps_spy;
    private STOMPGateway gateway;
    private STOMPSubscriptionManager subManager_spy;
    private int port = 61634;
    private String host = "localhost";


    @BeforeMethod
    public void setUp() throws Exception {
        STOMPServer server = new STOMPServer();
        STOMPProtocolServer ps = new STOMPProtocolServer(host, port);
        STOMPSubscriptionManager subManager = new STOMPSubscriptionManager();

        subManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        subManager_spy = Mockito.spy(subManager);
        server.setSubscriptionManager(subManager_spy);

        server_spy = Mockito.spy(server);
        ps_spy = Mockito.spy(ps);

        startGateway(host, port);
    }

    private void startGateway(String host, int port) throws Exception {
        server_spy.setProtocolServer(ps_spy);
        gateway = no.ntnu.okse.protocol.stomp.common.Gateway.initialize(host, port);
        gateway.connect();
        server_spy.gateway = gateway;
    }

    @AfterMethod
    public void tearDown() throws Exception {
        server_spy.stopServer();
        server_spy = null;
        ps_spy = null;
        gateway.shutdown();
        gateway = null;
    }

    @Test
    public void sendMessage() throws Exception {
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
        assertEquals(port, gateway.getPort());
        assertNotNull(null, gateway);
    }

    @Test
    public void stopServer() throws Exception {
        assertEquals(port, gateway.getPort());
        server_spy.stopServer();
        Mockito.verify(gateway).shutdown();
        assertEquals(null, server_spy.gateway);
    }

    @Test
    public void stopServerCatchException() throws Exception {
        assertEquals(port, server_spy.gateway.getPort());
        server_spy.stopServer();
        Mockito.doThrow(new Exception("Test Exception")).when(gateway).shutdown();
        Mockito.verify(gateway).shutdown();
    }
}
