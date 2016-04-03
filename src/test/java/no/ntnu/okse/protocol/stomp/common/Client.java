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
package no.ntnu.okse.protocol.stomp.common;

import static asia.stampy.common.message.StompMessageType.ABORT;
import static asia.stampy.common.message.StompMessageType.ACK;
import static asia.stampy.common.message.StompMessageType.BEGIN;
import static asia.stampy.common.message.StompMessageType.COMMIT;
import static asia.stampy.common.message.StompMessageType.NACK;
import static asia.stampy.common.message.StompMessageType.SEND;
import static asia.stampy.common.message.StompMessageType.SUBSCRIBE;
import static asia.stampy.common.message.StompMessageType.UNSUBSCRIBE;

import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.client.message.subscribe.SubscribeHeader.Ack;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.examples.system.client.netty.SystemNettyClientInitializer;
import asia.stampy.server.message.error.ErrorMessage;
import asia.stampy.server.message.receipt.ReceiptMessage;
import no.ntnu.okse.protocol.stomp.MessageListener;

public class Client {

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
    public void init(StampyMessageListener listener) throws Exception {
        setGateway(SystemNettyClientInitializer.initialize());
        getGateway().setPort(61613);

        if(listener != null)
            addMessageListener(listener);
        gateway.connect();
    }

    public void addMessageListener(StampyMessageListener listener){
        gateway.addMessageListener(listener);
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

    public void testSubscription(String id, String dest) throws Exception {
        SubscribeMessage message = new SubscribeMessage(dest, id);
        message.getHeader().setAck(Ack.clientIndividual);
        gateway.broadcastMessage(message);
    }

    private void goodConnect(String user) throws InterceptException {
        ConnectMessage message = new ConnectMessage("localhost");
        message.getHeader().setLogin(user);
        message.getHeader().setPasscode("pass");
        message.getHeader().setHeartbeat(500, 1000);

        getGateway().broadcastMessage(message);
    }


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

    public void testMessage() throws Exception{
//        sendMessage(StompMessageType.SEND, "Gabrielb");
        String id = "gabrielb";
        SendMessage message = new SendMessage("test", id);
        message.getHeader().setReceipt(id);
        message.getHeader().setDestination("test");
        message.setBody("Test");
        getGateway().broadcastMessage(message);
    }

    public void testUnsubscribe(String id) throws InterceptException {
        UnsubscribeMessage message = new UnsubscribeMessage(id);
        getGateway().broadcastMessage(message);
    }
}