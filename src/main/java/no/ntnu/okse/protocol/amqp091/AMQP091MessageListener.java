package no.ntnu.okse.protocol.amqp091;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.Publisher;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.core.subscription.SubscriptionService;
import org.apache.log4j.Logger;
import org.ow2.joram.mom.amqp.AMQPMessageListener;
import org.ow2.joram.mom.amqp.messages.*;

import java.util.logging.Formatter;

public class AMQP091MessageListener implements AMQPMessageListener {

    private static Logger log = Logger.getLogger(AMQP091MessageListener.class);

    private final AMQP091ProtocolServer amqpProtocolServer;

    public AMQP091MessageListener(AMQP091ProtocolServer amqp091ProtocolServer) {
        this.amqpProtocolServer = amqp091ProtocolServer;
    }

    @Override
    public void onConnect(ConnectMessage connectMessage) {
        log.debug(String.format("(%s:%d) connected",
                connectMessage.getHost(), connectMessage.getPort()
        ));
    }

    @Override
    public void onDisconnect(DisconnectMessage disconnectMessage) {
        log.debug(String.format("(%s:%d) disconnected",
                disconnectMessage.getHost(), disconnectMessage.getPort()
        ));
    }

    @Override
    public void onMessageReceived(MessageReceived messageReceived) {
        String message = new String(messageReceived.getBody());
        String topic = messageReceived.getExchange();
        String host = messageReceived.getHost();
        int port = messageReceived.getPort();
        log.debug(String.format("Message received from %s:%d on topic %s with contents \"%s\"",
                host, port, topic, message
        ));

        String protocolServerType = amqpProtocolServer.getProtocolServerType();
        Publisher pub = new Publisher(topic, host, port, protocolServerType);

        MessageService.getInstance().distributeMessage(new Message(message, topic, pub, protocolServerType));
    }

    @Override
    public void onSubscribe(SubscribeMessage subscribeMessage) {
        log.debug(String.format("%s:%d subscribed on topic %s",
                subscribeMessage.getHost(), subscribeMessage.getPort(), subscribeMessage.getExchange()
        ));
    }

    @Override
    public void onUnsubscribe(UnsubscribeMessage unsubscribeMessage) {
        log.debug(String.format("%s:%d unubscribed on topic %s",
                unsubscribeMessage.getHost(), unsubscribeMessage.getPort(), unsubscribeMessage.getExchange()
        ));
    }
}
