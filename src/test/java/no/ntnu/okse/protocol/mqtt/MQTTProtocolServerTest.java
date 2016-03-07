package no.ntnu.okse.protocol.mqtt;

import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class MQTTProtocolServerTest {

	@Test
	public void boot_changeState() {
		MQTTProtocolServer mqtt = getInstance();
		assertFalse(mqtt.isRunning());
		mqtt.boot();
		assertTrue(mqtt.isRunning());
		assertTrue(mqtt.isRunning());
	}

	@Test
	public void stopServer_startAndStop() throws InterruptedException {
		MQTTProtocolServer mqtt = getInstance();
		mqtt.boot();
		// Wait for server to boot
		Thread.sleep(1000);
		assertTrue(mqtt.isRunning());
		mqtt.stopServer();
		assertFalse(mqtt.isRunning());
	}

	@Test
	public void getProtocolServerType() {
		assertNotNull(getInstance().getProtocolServerType());
	}

	private MQTTProtocolServer getInstance() {
		return new MQTTProtocolServer("localhost", 1234);
	}


}
