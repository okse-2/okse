package no.ntnu.okse.protocol.mqtt;

import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.*;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.Publisher;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.mockito.*;

import static org.testng.Assert.*;


public class MQTTServerTest {



	enum Status {
		CONNECT,
		DISCONNECT,
		PUBLISH,
		SUBSCRIBE,
		UNSUBSCRIBE;
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
	MQTTProtocolServer ps;

	@BeforeTest
	public void setUp() {
		String host = "localhost";
		int port = 1234;
		ps = new MQTTProtocolServer(host, port);
		mqtt = new MQTTServer(ps, host, port);
	}

	@AfterTest
	public void tearDown() {
		mqtt = null;
		status = null;
	}


	@Test
	public void sendMessage() {
	}

	@Test
	public void receiveMessage() {

	}

	private MQTTServer getInstance(){
		return mqtt;
	}
}
