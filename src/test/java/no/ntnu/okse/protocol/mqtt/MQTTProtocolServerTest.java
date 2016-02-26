package no.ntnu.okse.protocol.mqtt;

import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class MQTTProtocolServerTest {

	@Test
	public void getInstance_singleTonTest() {
		MQTTProtocolServer mqtt = MQTTProtocolServer.getInstance();
		assertSame(MQTTProtocolServer.getInstance(), mqtt, "getInstance should always return the same object");
	}

	@Test
	public void getInstance_withParameters() {
		MQTTProtocolServer mqtt = MQTTProtocolServer.getInstance("localhost", 7000);
		assertSame(MQTTProtocolServer.getInstance(), mqtt, "getInstance should always return the same object");
	}
}
