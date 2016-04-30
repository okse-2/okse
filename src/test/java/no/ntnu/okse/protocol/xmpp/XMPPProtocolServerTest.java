package no.ntnu.okse.protocol.xmpp;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.protocol.mqtt.MQTTProtocolServer;
import no.ntnu.okse.protocol.mqtt.MQTTServer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.logging.Logger;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by asus on 30/04/2016.
 */
public class XMPPProtocolServerTest {

    @Mock
    private XMPPServer xmppServer;
    @InjectMocks
    XMPPProtocolServer xmppProtocolServer = new XMPPProtocolServer("localhost", 1234);

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void boot_changeState() {
        assertFalse(xmppProtocolServer.isRunning());
        xmppProtocolServer.boot();
        assertTrue(xmppProtocolServer.isRunning());
    }

    @Test
    public void stopServer_startAndStop() throws InterruptedException {
        xmppProtocolServer.boot();
        // Wait for server to boot
        Thread.sleep(1000);
        assertTrue(xmppProtocolServer.isRunning());
        xmppProtocolServer.stopServer();
        assertFalse(xmppProtocolServer.isRunning());
    }

    @Test
    public void getProtocolServerType() {
        assertNotNull(xmppProtocolServer.getProtocolServerType());
    }

    @Test
    public void sendMessage() {
        Message message = createMessage();
        xmppProtocolServer.sendMessage(message);
        Mockito.verify(xmppServer).sendMessage(message);
    }

    private Message createMessage() {
        return new Message("Message", "Topic", null, "mqtt");
    }

}
