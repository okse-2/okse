package no.ntnu.okse.clients.stomp;

import asia.stampy.client.listener.validate.ClientMessageValidationListener;
import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.client.message.subscribe.SubscribeHeader;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.client.mina.ClientMinaMessageGateway;
import asia.stampy.client.mina.RawClientMinaHandler;
import asia.stampy.client.mina.connected.MinaConnectedMessageListener;
import asia.stampy.client.mina.disconnect.MinaDisconnectListenerAndInterceptor;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.SecurityMessageListener;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.server.message.message.MessageMessage;
import no.ntnu.okse.clients.TestClient;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


public class StompClient implements TestClient {

    private static Logger log = Logger.getLogger(StompClient.class);
    private final HeartbeatContainer heartbeatContainer;
    private final ClientMinaMessageGateway gateway;

    private int receiptId = 1;
    private int subscriptionId = 1;
    private Map<String, String> subscriptionMap = new HashMap<>();
    private StompCallback callback;

    public StompClient() {
        this("localhost", 61613);
    }

    public StompClient(String host, int port) {
        heartbeatContainer = new HeartbeatContainer();
        gateway = new ClientMinaMessageGateway();
        gateway.setPort(port);
        gateway.setHost(host);
        gateway.setHeartbeat(1000);
        initialize();
    }

    private void initialize() {
        RawClientMinaHandler handler = new RawClientMinaHandler();
        handler.setHeartbeatContainer(heartbeatContainer);
        handler.setGateway(gateway);

        gateway.addMessageListener(new DummySecurityListener());
        gateway.addMessageListener(new ClientMessageValidationListener());

        MinaConnectedMessageListener cml = new MinaConnectedMessageListener();
        cml.setHeartbeatContainer(heartbeatContainer);
        cml.setGateway(gateway);
        gateway.addMessageListener(cml);

        MinaDisconnectListenerAndInterceptor disconnect = new MinaDisconnectListenerAndInterceptor();
        disconnect.setCloseOnDisconnectMessage(false);
        gateway.addMessageListener(disconnect);
        gateway.addOutgoingMessageInterceptor(disconnect);
        disconnect.setGateway(gateway);

        gateway.setHandler(handler);
    }

    @Override
    public void connect() {
        // Ack listener
        gateway.addMessageListener(new StampyMessageListener() {

            @Override
            public void messageReceived(StampyMessage<?> message, HostPort hostPort) {
                if(message.getMessageType().equals(StompMessageType.MESSAGE)) {
                    MessageMessage messageMessage = (MessageMessage) message;
                    AckMessage ack = new AckMessage(messageMessage.getHeader().getAck());
                    callback.messageReceived(messageMessage);
                    try {
                        broadcast(ack);
                    } catch (InterceptException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public boolean isForMessage(StampyMessage<?> message) {
                return true;
            }

            @Override
            public StompMessageType[] getMessageTypes() {
                return StompMessageType.values();
            }
        });
        try {
            gateway.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ConnectMessage message = new ConnectMessage("localhost");

        try {
            broadcast(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        DisconnectMessage message = new DisconnectMessage();
        try {
            broadcast(message);
            gateway.shutdown();
        } catch (Exception e) {
            log.error("Disconnect failed", e);
        }
    }

    @Override
    public void subscribe(String topic) {
        String subscription = String.valueOf(subscriptionId++);
        subscriptionMap.put(topic, subscription);
        SubscribeMessage message = new SubscribeMessage(topic, subscription);
        message.getHeader().setAck(SubscribeHeader.Ack.clientIndividual);
        try {
            broadcast(message);
        } catch (InterceptException e) {
            log.error("Failed to subscribe", e);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        UnsubscribeMessage message = new UnsubscribeMessage(subscriptionMap.get(topic));
        try {
            broadcast(message);
        } catch (InterceptException e) {
            log.error("Failed to unsubscribe", e);
        }
    }

    @Override
    public void publish(String topic, String content) {
        SendMessage message = new SendMessage(topic, String.valueOf(receiptId++));
        message.setBody(content);
        try {
            broadcast(message);
        } catch (InterceptException e) {
            log.error("Failed to publish on topic " + topic, e);
        }
    }

    public void setCallback(StompCallback callback) {
        this.callback = callback;
    }

    private void broadcast(StampyMessage message) throws InterceptException {
        gateway.broadcastMessage(message);
    }
}
