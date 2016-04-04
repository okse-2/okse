package no.ntnu.okse.protocol.amqp091;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.Publisher;
import org.ow2.joram.mom.amqp.AMQPMessageListener;
import org.ow2.joram.mom.amqp.messages.*;

public class AMQP091MessageListener implements AMQPMessageListener {


    private final AMQP091ProtocolServer amqpProtocolServer;

    public AMQP091MessageListener(AMQP091ProtocolServer amqp091ProtocolServer) {
        this.amqpProtocolServer = amqp091ProtocolServer;
    }

    @Override
    public void onConnect(ConnectMessage connectMessage) {

    }

    @Override
    public void onDisconnect(DisconnectMessage disConnectMessage) {

    }

    @Override
    public void onMessageReceived(MessageReceived messageReceived) {
        String message = new String(messageReceived.getBody());
        String topic = messageReceived.getExchange();
        String host = messageReceived.getHost();
        int port = messageReceived.getPort();

        String protocolServerType = amqpProtocolServer.getProtocolServerType();
        Publisher pub = new Publisher(topic, host, port, protocolServerType);

        MessageService.getInstance().distributeMessage(new Message(message, topic, pub, protocolServerType));
    }

    @Override
    public void onSubscribe(SubscribeMessage subscribeMessage) {

    }

    @Override
    public void onUnsubscribe(UnsubscribeMessage unsubscribeMessage) {

    }
}
