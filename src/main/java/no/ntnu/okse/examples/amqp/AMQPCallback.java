package no.ntnu.okse.examples.amqp;

import org.apache.qpid.proton.message.Message;

public interface AMQPCallback {
    void onReceive(Message message);
}
