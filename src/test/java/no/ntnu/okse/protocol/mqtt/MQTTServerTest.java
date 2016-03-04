package no.ntnu.okse.protocol.mqtt;

import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.*;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.topic.Topic;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class MQTTServerTest {

	enum Status {
		CONNECT,
		DISCONNECT,
		PUBLISH,
		SUBSCRIBE,
		UNSUBSCRIBE
	}

	private static Status status;
	private static InterceptAbstractMessage interceptMessage;

	private static final class MockObserver implements InterceptHandler {
		@Override
		public void onConnect(InterceptConnectMessage interceptConnectMessage) {
			status = Status.CONNECT;
		}

		@Override
		public void onDisconnect(InterceptDisconnectMessage interceptDisconnectMessage) {
			status = Status.DISCONNECT;
		}

		@Override
		public void onPublish(InterceptPublishMessage interceptPublishMessage) {
			status = Status.PUBLISH;
		}

		@Override
		public void onSubscribe(InterceptSubscribeMessage interceptSubscribeMessage) {
			status = Status.SUBSCRIBE;
		}

		@Override
		public void onUnsubscribe(InterceptUnsubscribeMessage interceptUnsubscribeMessage) {
			status = Status.UNSUBSCRIBE;
		}
	}

	MQTTServer mqtt;

	@BeforeTest
	public void setUp() {
		mqtt = new MQTTServer();
	}

	@AfterTest
	public void tearDown() {
		mqtt = null;
		status = null;
	}

	@Test
	public void init() {
//		assertFalse(mqtt.isRunning());
//		mqtt.init("localhost", 1234);
//		assertTrue(mqtt.isRunning());
	}

	@Test
	public void sendMessage() {
		Message message = new Message("Test message", "MQTT", null, "MQTT");
		mqtt.sendMessage(message);
	}

	@Test
	public void receiveMessage() {

	}
}
