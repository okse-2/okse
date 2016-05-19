package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.*;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.examples.system.server.SystemAcknowledgementHandler;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.netty.Boilerplate;
import asia.stampy.server.netty.ServerNettyMessageGateway;
import io.moquette.server.Server;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.Subscriber;
import no.ntnu.okse.protocol.stomp.listeners.*;
import no.ntnu.okse.protocol.stomp.listeners.ErrorInterceptor;
import no.ntnu.okse.protocol.stomp.listeners.MessageListener;
import org.apache.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.*;

public class STOMPServer extends Server {
    private static STOMPSubscriptionManager subscriptionManager;
    public ServerNettyMessageGateway gateway;
    private static STOMPProtocolServer ps;
    private Logger log;

    /**
     * Sets up the logger when we create a new instance of this class
     */
    public STOMPServer(){
        log = Logger.getLogger(STOMPProtocolServer.class.getName());
    }

    /**
     * Sets the subscription manager for the class
     * @param subscriptionManager the subscription manager instance
     */
    public void setSubscriptionManager(STOMPSubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    /**
     * Initialises the gateway and sets handlers, listeners, host and port.
     * Also sets many other settings
     * @param host the host to bind to
     * @param port the port to bind to
     * @return the gateway object
     */
    private ServerNettyMessageGateway initialize(String host, int port) {
        ServerNettyMessageGateway gateway = new ServerNettyMessageGateway();

        ErrorInterceptor errorInterceptor = new ErrorInterceptor();
        errorInterceptor.setProtocolServer(ps);

        DisconnectListener disconnectListener = new DisconnectListener();
        disconnectListener.setSubscriptionManager(subscriptionManager);
        disconnectListener.setGateway(gateway);

        SystemAcknowledgementHandler sys = new SystemAcknowledgementHandler();

        gateway.addMessageListener(new IDontNeedSecurity());

        Boilerplate b = new Boilerplate();
        b.init(gateway, host, port, errorInterceptor, disconnectListener, sys);

        addGatewayListenersAndHandlers(gateway);

        return gateway;
    }

    /**
     * Adds gateway listeners to the gateway object. These listeners subscribe to particular messages
     * and perform some action when these messages are received.
     * This method is called as the last method before we return the
     * gateway object in the initialize method
     *
     * We prefer that every listener performs a specific action, for example a subscriber handler would
     * only handle subscriptions and no other aspects of the publish/subscribe pattern, unsubscribing for example.
     *
     * @param gateway the gateway object to add listeners to
     */
    private void addGatewayListenersAndHandlers(ServerNettyMessageGateway gateway){
        no.ntnu.okse.protocol.stomp.listeners.MessageListener messageListener = new MessageListener();
        SubscriptionListener subListener = new SubscriptionListener();
        UnSubscriptionListener unsubListener = new UnSubscriptionListener();
        MIMEtypeListener mimeTypeListener = new MIMEtypeListener();
        IncrementTotalRequestsListener incrementTotalRequestsListener = new IncrementTotalRequestsListener();
        ErrorListener errorListener = new ErrorListener();

        subListener.setSubscriptionManager(subscriptionManager);
        unsubListener.setSubscriptionManager(subscriptionManager);

        messageListener.setProtocolServer(ps);
        errorListener.setProtocolServer(ps);
        incrementTotalRequestsListener.setProtocolServer(ps);

        messageListener.setMessageService(MessageService.getInstance());

        gateway.addOutgoingMessageInterceptor(errorListener);
        gateway.addMessageListener(mimeTypeListener);
        gateway.addMessageListener(subListener);
        gateway.addMessageListener(unsubListener);
        gateway.addMessageListener(messageListener);
        gateway.addMessageListener(incrementTotalRequestsListener);
    }

    /**
     * Inits the actual gateway and all its listeners, also binds the to a specific host and port
     * @param host the host to bind to
     * @param port the port to bind to
     * @throws Exception
     */
    public void init(String host, int port) throws Exception {
        gateway = initialize(host, port);
        gateway.connect();
    }

    /**
     * Sets the protocolServer for the class
     * @param ps the protocol server to be set
     */
    public void setProtocolServer(STOMPProtocolServer ps){
        this.ps = ps;
    }

    /**
     * Sends the message to any subscriber that is subscribed to the topic that the message was sent to
     * @param message is the message that is sent from OKSE core
     * */
    public void sendMessage(@NotNull Message message) {
        log.debug("OKSE has received a message, please redistribute!");

        HashMap<String, Subscriber> subs = subscriptionManager.getAllSubscribersForTopic(message.getTopic());

        Iterator it = subs.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            String key = (String) pair.getKey();

            Subscriber sub = subs.get(key);

            //TODO: Do we also have to change the message id?
            MessageMessage msg = createSTOMPMessage(message, key);
            try {
                getGateway().sendMessage((StampyMessage<?>) msg, new HostPort(sub.getHost(), sub.getPort()));
                ps.incrementTotalMessagesSent();
            } catch (InterceptException e) {
                ps.incrementTotalErrors();
                log.error("Error happened when STOMP tried to send a message to the client", e);
            }
        }
    }

    /**
     * Creates a STOMP message from an OKSE message
     * @param msg - OKSE message to convert to STOMP message
     * @param id - Specific ID for the message
     * @return
     */
    private MessageMessage createSTOMPMessage(Message msg, String id){
        String msgId = msg.getMessageID();
        MessageMessage message = new MessageMessage(msg.getTopic(), msgId, id);

        //Adds all the user defined headers to the STOMP message
        HashMap<String, String> attributes = msg.getAttributes();
        Iterator it = attributes.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            String key = (String) pair.getKey();
            message.getHeader().addHeader(key, attributes.get(key));
        }

        message.setBody(msg.getMessage());
        message.getHeader().setAck(msgId);
        return message;
    }

    /**
     * Stops the server and sets gateway to null
     */
    public void stopServer(){
        //TODO: This needs to be implemented, will be left like this because of demo purposes
        log.info("Shutting down STOMP server");
        try {
            gateway.shutdown();
            gateway = null;
        } catch (Exception e) {
            log.error("Exception when trying to shutdown the server", e);
        }
    }

    public ServerNettyMessageGateway getGateway() {
        return gateway;
    }
}
