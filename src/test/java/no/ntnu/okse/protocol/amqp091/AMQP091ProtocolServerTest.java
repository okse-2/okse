package no.ntnu.okse.protocol.amqp091;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.*;

@Test
public class AMQP091ProtocolServerTest {
    private AMQP091ProtocolServer protocolServer;
    @Mock
    private AMQP091Service amqpService;
    @Mock
    private SubscriptionService subscriptionService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        protocolServer = spy(new AMQP091ProtocolServer("localhost", 32121));
        protocolServer.setAmqpService(amqpService);
        protocolServer.setSubscriptionService(subscriptionService);
    }

    @AfterMethod
    public void tearDown() {
        protocolServer = null;
    }

    public void run() {
        protocolServer.run();
        verify(amqpService).start();
    }

    public void boot() {
        protocolServer.setAmqpService(null);
        doNothing().when(protocolServer).run();
        protocolServer.boot();
        verify(protocolServer).run();
    }

    public void run_stop() {
        protocolServer.run();
        verify(amqpService).start();
        protocolServer.stopServer();
        verify(amqpService).stop();
    }

    public void stop_notStarted() {
        protocolServer.setAmqpService(null);
        protocolServer.stopServer();
        verify(amqpService, never()).stop();
    }

    public void getProtocolServerType() {
        assertEquals(protocolServer.getProtocolServerType(), "amqp091");
    }

    public void sendMessage_noSubscribers() {
        Message message = new Message("Message body", "topic", null, protocolServer.getProtocolServerType());
        protocolServer.sendMessage(message);
        verify(protocolServer, never()).incrementTotalMessagesSent();
    }

    public void sendMessage_Subscribers() {
        Message message = new Message("Message body", "topic", null, protocolServer.getProtocolServerType());
        protocolServer.sendMessage(message);
        verify(protocolServer, never()).incrementTotalMessagesSent();
    }
}
