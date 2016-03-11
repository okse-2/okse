package no.ntnu.okse.protocol;

import no.ntnu.okse.protocol.amqp.AMQProtocolServer;
import no.ntnu.okse.protocol.wsn.WSNotificationServer;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ProtocolServerFactory {

    public static ProtocolServer create(Node node) {
        NamedNodeMap attr = node.getAttributes();
        if(attr.getNamedItem("type") == null)
            return null;

        switch(attr.getNamedItem("type").getNodeValue()) {
            case "amqp":
                return createAMQP(attr);
            case "wsn":
                return createWSN(attr);
            default:
                return null;
        }
    }

    private static boolean stringToBoolean(String bool, boolean defaulte) {
        if(bool.equals("true")) return true;
        else if(bool.equals("false")) return false;
        else return defaulte;
    }

    private static int stringToPort(String port, int defaulte) {
        try {
            int p = Integer.parseInt(port);
            if(p >= 1 && p <= 65535) return p;
            else return defaulte;
        } catch(NumberFormatException e) {
            return defaulte;
        }
    }

    private static int stringToInt(String i, int defaulte) {
        try {
            int p = Integer.parseInt(i);
            return p;
        } catch(NumberFormatException e) {
            return defaulte;
        }
    }

    private static AMQProtocolServer createAMQP(NamedNodeMap attr) {
        final String DEFAULT_HOST = "0.0.0.0";
        final int DEFAULT_PORT = 5672;
        final boolean DEFAULT_SASL = true;
        final boolean DEFAULT_QUEUE = false;

        String host = attr.getNamedItem("host") != null ?
                attr.getNamedItem("host").getNodeValue() :
                DEFAULT_HOST;

        int port = attr.getNamedItem("port") != null ?
                stringToPort(attr.getNamedItem("port").getNodeValue(), DEFAULT_PORT):
                DEFAULT_PORT;

        boolean sasl = attr.getNamedItem("sasl") != null ?
                stringToBoolean(attr.getNamedItem("sasl").getNodeValue(), DEFAULT_SASL) :
                DEFAULT_SASL;

        boolean queue = attr.getNamedItem("queue") != null ?
                stringToBoolean(attr.getNamedItem("queue").getNodeValue(), DEFAULT_QUEUE) :
                DEFAULT_QUEUE;

        return new AMQProtocolServer(host, port, queue, sasl);
    }

    private static WSNotificationServer createWSN(NamedNodeMap attr) {
        final String DEFAULT_HOST = "0.0.0.0";
        final int DEFAULT_PORT = 61000;
        final int DEFAULT_TIMEOUT = 5;
        final int DEFAULT_POOL_SIZE = 50;
        final String DEFAULT_WRAPPER_NAME = "Content";
        final boolean DEFAULT_NAT = false;
        final String DEFAULT_WAN_HOST = "0.0.0.0";
        final int DEFAULT_WAN_PORT = 61000;

        String host = attr.getNamedItem("host") != null ?
                attr.getNamedItem("host").getNodeValue() :
                DEFAULT_HOST;

        int port = attr.getNamedItem("port") != null ?
                stringToPort(attr.getNamedItem("port").getNodeValue(), DEFAULT_PORT) :
                DEFAULT_PORT;

        int timeout = attr.getNamedItem("timeout") != null ?
                stringToInt(attr.getNamedItem("timeout").getNodeValue(), DEFAULT_TIMEOUT) :
                DEFAULT_TIMEOUT;

        int pool_size = attr.getNamedItem("pool_size") != null ?
                stringToInt(attr.getNamedItem("pool_size").getNodeValue(), DEFAULT_POOL_SIZE) :
                DEFAULT_POOL_SIZE;

        String wrapper_name = attr.getNamedItem("wrapper_name") != null ?
                attr.getNamedItem("wrapped_name").getNodeValue() :
                DEFAULT_WRAPPER_NAME;

        boolean nat = attr.getNamedItem("nat") != null ?
                stringToBoolean(attr.getNamedItem("nat").getNodeValue(), DEFAULT_NAT) :
                DEFAULT_NAT;

        String wan_host = attr.getNamedItem("wan_host") != null ?
                attr.getNamedItem("wan_host").getNodeValue() :
                DEFAULT_WAN_HOST;

        int wan_port = attr.getNamedItem("wan_port") != null ?
                stringToInt(attr.getNamedItem("wan_port").getNodeValue(), DEFAULT_WAN_PORT) :
                DEFAULT_WAN_PORT;

        return new WSNotificationServer(
                host, port, Integer.toUnsignedLong(timeout), pool_size,
                wrapper_name, nat, wan_host, wan_port);
    }
}

