package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.interceptor.InterceptException;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.stomp.commons.STOMPGateway;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
        try{
            gateway.shutdown();
        }catch(Exception ignored){}
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
    public void incrementTotalErrors() throws InterceptException {
        subManager_spy.addSubscriber(new Subscriber("localhost", 61613, "testing", "stomp"), "ogdans3");


        Message msg = new Message("testing", "testing", null, "stomp");
        msg.setAttribute("test", "user defined attribute");

        ArgumentCaptor<StampyMessage> stampy = ArgumentCaptor.forClass(StampyMessage.class);
        ArgumentCaptor<HostPort> hp = ArgumentCaptor.forClass(HostPort.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                throw new InterceptException("Intercepting to increment total number of errors");
            }
        }).when(gateway).sendMessage(stampy.capture(), hp.capture());
        server_spy.sendMessage(msg);
    }

    @Test
    public void init() throws Exception {
        assertEquals(port, gateway.getPort());
        assertNotNull(null, gateway);
    }

    @Test
    public void getGateway(){
        assertEquals(gateway, server_spy.getGateway());
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
        Mockito.doThrow(new Exception("Test Exception")).when(gateway).shutdown();
        server_spy.stopServer();
    }
}
