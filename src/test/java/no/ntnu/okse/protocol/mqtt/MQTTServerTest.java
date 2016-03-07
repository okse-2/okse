package no.ntnu.okse.protocol.mqtt;

import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.*;
import io.moquette.parser.proto.messages.AbstractMessage;
import io.moquette.parser.proto.messages.PublishMessage;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.subscription.Publisher;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;

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
		MQTTServer mqtt = getInstance();
		Message message = new Message("Test message", "MQTT", null, "MQTT");
		PublishMessage msg = createMQTTMessage();

		MQTTServer spy = Mockito.spy(mqtt);

		//Test that internalPublish is called with the correct parameter
		Mockito.doReturn(msg).when(spy).createMQTTMessage(message);
		Mockito.doNothing().when(spy).internalPublish(msg);
		spy.sendMessage(message);
		Mockito.verify(spy, Mockito.atLeastOnce()).internalPublish(msg);
	}

	@Test
	public void receiveMessage() {

	}

	private MQTTServer getInstance(){
		return mqtt;
	}
	private PublishMessage createMQTTMessage(){
		PublishMessage msg = new PublishMessage();
		ByteBuffer payload = ByteBuffer.wrap( "This is a test".getBytes() );

		String topicName = "Test";

		msg.setPayload( payload );
		msg.setTopicName( topicName );
		msg.setQos(AbstractMessage.QOSType.LEAST_ONE);
		return msg;

	}
}
