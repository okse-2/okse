package no.ntnu.okse.clients.amqp091;

public interface AMQP091Callback {
    void messageReceived(String topic, String message);
}
