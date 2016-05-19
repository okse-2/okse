package no.ntnu.okse.clients.amqp;

import org.apache.qpid.proton.message.Message;

public interface AMQPCallback {
    void onReceive(Message message);
}
