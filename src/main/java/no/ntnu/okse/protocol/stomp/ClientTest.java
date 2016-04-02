/*
 * Copyright (C) 2013 Burton Alexander
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
package no.ntnu.okse.protocol.stomp;

import static asia.stampy.common.message.StompMessageType.ABORT;
import static asia.stampy.common.message.StompMessageType.ACK;
import static asia.stampy.common.message.StompMessageType.BEGIN;
import static asia.stampy.common.message.StompMessageType.COMMIT;
import static asia.stampy.common.message.StompMessageType.NACK;
import static asia.stampy.common.message.StompMessageType.SEND;
import static asia.stampy.common.message.StompMessageType.SUBSCRIBE;
import static asia.stampy.common.message.StompMessageType.UNSUBSCRIBE;
import asia.stampy.client.message.abort.AbortMessage;
import asia.stampy.client.message.ack.AckMessage;
import asia.stampy.client.message.begin.BeginMessage;
import asia.stampy.client.message.commit.CommitMessage;
import asia.stampy.client.message.connect.ConnectHeader;
import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.client.message.nack.NackMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.client.message.stomp.StompMessage;
import asia.stampy.client.message.subscribe.SubscribeHeader.Ack;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.examples.system.client.netty.SystemNettyClientInitializer;
import asia.stampy.examples.system.server.SystemLoginHandler;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.message.message.MessageMessage;
import asia.stampy.server.message.receipt.ReceiptMessage;

import java.util.Random;

public class ClientTest {

    private static final String CANNOT_BE_LOGGED_IN = "cannot be logged in";

    private static final String IS_ALREADY_LOGGED_IN = "is already logged in";

    private static final String ONLY_STOMP_VERSION_1_2_IS_SUPPORTED = "Only STOMP version 1.2 is supported";

    private static final String LOGIN_AND_PASSCODE_NOT_SPECIFIED = "login and passcode not specified";

    private static final String NOT_LOGGED_IN = "Not logged in";

    private static final StompMessageType[] CLIENT_TYPES = { ACK, NACK, SEND, ABORT, BEGIN, COMMIT, SUBSCRIBE,
            UNSUBSCRIBE };

    private AbstractStampyMessageGateway gateway;

    private ErrorMessage error;

    private ReceiptMessage receipt;

    private Object waiter = new Object();

    private boolean connected;

    private int messageCount;

    private HostPort hostPort;

    /**
     * Inits the.
     *
     * @throws Exception
     *           the exception
     */
    public void init() throws Exception {
        setGateway(SystemNettyClientInitializer.initialize());
        getGateway().setPort(5551);

        gateway.addMessageListener(new MessageListener());
/*        gateway.addMessageListener(new StampyMessageListener() {

            @Override
            public void messageReceived(StampyMessage<?> message, HostPort hostPort) throws Exception {
//                ClientTest.this.hostPort = hostPort;
                switch (message.getMessageType()) {
                    case CONNECTED:
                        connected = true;
                        break;
                    case ERROR:
                        System.out.println("=======Error: " + message);
                        break;
                    case MESSAGE:
//                        onMessage((MessageMessage) message);
                        System.out.println("=======Message: " + message);
                        break;
                    case RECEIPT:
                        System.out.println("=======Receipt: " + message);
                        break;
                    default:
                        break;

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
*/

        gateway.connect();
    }

    /**
     * Test connect.
     *
     * @throws Exception
     *           the exception
     */
    public void testConnect(String user) throws Exception {
        ConnectMessage message = new ConnectMessage("1.2", "localhost");
        message.getHeader().setLogin(user);
        message.getHeader().setPasscode("pass");
//        getGateway().sendMessage(message, new HostPort("localhost", 5551));
        getGateway().broadcastMessage(message);
    }

    public void testMessage() throws Exception{
//        sendMessage(StompMessageType.SEND, "Gabrielb");
        String id = "gabrielb";
        SendMessage message = new SendMessage("test", id);
        message.getHeader().setReceipt(id);
        message.getHeader().setDestination("test");
        message.setBody("Test");
//        getGateway().sendMessage(message, new HostPort("localhost", 5551));
        getGateway().broadcastMessage(message);
    }

    /**
     * Test login.
     *
     * @throws Exception
     *           the exception
     */
    public void testLogin(String user) throws Exception {
        goodConnect(user);
    }

    public void testSubscription() throws Exception {
        SubscribeMessage message = new SubscribeMessage("test", "subscription");
        message.getHeader().setAck(Ack.clientIndividual);
        gateway.broadcastMessage(message);
    }

    private void onMessage(MessageMessage message) throws InterceptException, InterruptedException {
        messageCount++;
        if (messageCount < 100) {
            AckMessage ack = new AckMessage(message.getHeader().getMessageId());
            gateway.broadcastMessage(ack);
        }
        if (messageCount == 100) {
            System.out.println("Received all expected messages from subscription");
            sendUnsubscribe("subscription");
            Thread.sleep(250);
            synchronized (waiter) {
                waiter.notifyAll();
            }
        } else if (messageCount > 100) {
            throw new IllegalArgumentException("Extra message received");
        }
    }

    private void goodConnect(String user) throws InterceptException {
        ConnectMessage message = new ConnectMessage("localhost");
        message.getHeader().setLogin(user);
        message.getHeader().setPasscode("pass");
        message.getHeader().setHeartbeat(500, 1000);

        getGateway().broadcastMessage(message);
    }

    private void sendMessage(StompMessageType type, String id) throws InterceptException {
        switch (type) {
            case ABORT:
                sendAbort(id);
                break;
            case ACK:
                sendAck(id);
                break;
            case BEGIN:
                sendNack(id);
                break;
            case COMMIT:
                sendCommit(id);
                break;
            case CONNECT:
                sendConnect(id);
                break;
            case DISCONNECT:
                sendDisconnect(id);
                break;
            case NACK:
                sendNack(id);
                break;
            case SEND:
                sendSend(id);
                break;
            case STOMP:
                sendStomp(id);
                break;
            case SUBSCRIBE:
                sendSubscribe(id);
                break;
            case UNSUBSCRIBE:
                sendUnsubscribe(id);
                break;
            default:
                break;

        }
    }

    private void sendUnsubscribe(String id) throws InterceptException {
        UnsubscribeMessage message = new UnsubscribeMessage(id);
        getGateway().broadcastMessage(message);
    }

    private void sendSubscribe(String id) throws InterceptException {
        SubscribeMessage message = new SubscribeMessage("over/there", id);
        getGateway().broadcastMessage(message);
    }

    private void sendStomp(String id) throws InterceptException {
        StompMessage message = new StompMessage(id);
        getGateway().broadcastMessage(message);
    }

    private void sendSend(String id) throws InterceptException {
        SendMessage message = new SendMessage("test", id);
        message.getHeader().setReceipt(id);
        getGateway().broadcastMessage(message);
    }

    private void sendDisconnect(String id) throws InterceptException {
        DisconnectMessage message = new DisconnectMessage();
        message.getHeader().setReceipt(id);
        getGateway().broadcastMessage(message);
    }

    private void sendConnect(String id) throws InterceptException {
        ConnectMessage message = new ConnectMessage(id);
        getGateway().broadcastMessage(message);
    }

    private void sendCommit(String id) throws InterceptException {
        CommitMessage message = new CommitMessage(id);
        message.getHeader().setReceipt(id);
        getGateway().broadcastMessage(message);
    }

    private void sendNack(String id) throws InterceptException {
        NackMessage message = new NackMessage(id);
        getGateway().broadcastMessage(message);
    }

    private void sendAck(String id) throws InterceptException {
        AckMessage message = new AckMessage(id);
        getGateway().broadcastMessage(message);
    }

    private void sendAbort(String id) throws InterceptException {
        AbortMessage message = new AbortMessage(id);
        message.getHeader().setReceipt(id);
        getGateway().broadcastMessage(message);
    }

    private void sendBegin(String id) throws InterceptException {
        BeginMessage message = new BeginMessage(id);
        message.getHeader().setReceipt(id);
        getGateway().broadcastMessage(message);
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    public ErrorMessage getError() {
        return error;
    }

    /**
     * Sets the error.
     *
     * @param error
     *          the new error
     */
    public void setError(ErrorMessage error) {
        this.error = error;
    }

    /**
     * Gets the gateway.
     *
     * @return the gateway
     */
    public AbstractStampyMessageGateway getGateway() {
        return gateway;
    }

    /**
     * Sets the gateway.
     *
     * @param gateway
     *          the new gateway
     */
    public void setGateway(AbstractStampyMessageGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * Gets the receipt.
     *
     * @return the receipt
     */
    public ReceiptMessage getReceipt() {
        return receipt;
    }

    /**
     * Sets the receipt.
     *
     * @param receipt
     *          the new receipt
     */
    public void setReceipt(ReceiptMessage receipt) {
        this.receipt = receipt;
    }

    public static void main(String[] args) throws Exception {
        ClientTest client = new ClientTest();
        client.init();
        client.testConnect(String.valueOf(0 + (int)(Math.random() * 500)));
        client.testSubscription();

    }
}