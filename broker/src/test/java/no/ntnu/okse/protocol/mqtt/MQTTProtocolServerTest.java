package no.ntnu.okse.protocol.mqtt;

import no.ntnu.okse.core.messaging.Message;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class MQTTProtocolServerTest {

    @InjectMocks
    MQTTProtocolServer mqtt = new MQTTProtocolServer("localhost", 1234);
    @Mock(name = "server")
    private MQTTServer mqttServerSpy;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void boot_changeState() {
        assertFalse(mqtt.isRunning());
        mqtt.boot();
        assertTrue(mqtt.isRunning());
    }

    @Test
    public void stopServer_startAndStop() throws InterruptedException {
        mqtt.boot();
        // Wait for server to boot
        Thread.sleep(1000);
        assertTrue(mqtt.isRunning());
        mqtt.stopServer();
        assertFalse(mqtt.isRunning());
    }

    @Test
    public void getProtocolServerType() {
        assertNotNull(mqtt.getProtocolServerType());
    }

    @Test
    public void sendMessage() {
        Message message = createMessage();
        mqtt.sendMessage(message);
        Mockito.verify(mqttServerSpy).sendMessage(message);
    }

    private Message createMessage() {
        return new Message("Message", "Topic", null, "mqtt");
    }
}
