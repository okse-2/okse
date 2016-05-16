package no.ntnu.okse.clients.amqp;

import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.messenger.Messenger;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example/test of the java Messenger/Message API.
 * Based closely qpid src/proton/examples/messenger/py/send.py
 *
 * @author mberkowitz@sf.org
 * @since 8/4/2013
 */
public class AMQPSend {

    private static Logger tracer = Logger.getLogger("proton.example");
    private String address = "amqp://m.fap.no:61050/amqp";
    private String subject = "NEWS!";
    private String[] bodies = new String[]{"Y0! Trond, zup?"};

    private static void usage() {
        System.err.println("Usage: send [-a ADDRESS] [-s SUBJECT] MSG+");
        System.exit(2);
    }

    private AMQPSend(String args[]) {
        int i = 0;
        while (i < args.length) {
            String arg = args[i++];
            if (arg.startsWith("-")) {
                if ("-a".equals(arg)) {
                    address = args[i++];
                } else if ("-s".equals(arg)) {
                    subject = args[i++];
                } else {
                    System.err.println("unknown option " + arg);
                    usage();
                }
            } else {
                --i;
                break;
            }
        }

        if (i != args.length) {
            bodies = Arrays.copyOfRange(args, i, args.length);
        }
    }

    private void run() {
        try {
            Messenger mng = Messenger.Factory.create();
            mng.start();
            Message msg = Message.Factory.create();
            msg.setAddress(address);
            System.out.println(msg.getAddress());
            if (subject != null) msg.setSubject(subject);
            System.out.println(msg.getSubject());
            for (String body : bodies) {
                System.out.println(body);
                msg.setBody(new AmqpValue(body));
                System.out.println(msg.getBody());
                mng.put(msg);
            }
            mng.send();
            mng.stop();
        } catch (Exception e) {
            tracer.log(Level.SEVERE, "proton error", e);
        }
    }

    public static void main(String args[]) {
        AMQPSend o = new AMQPSend(args);
        o.run();
    }
}
