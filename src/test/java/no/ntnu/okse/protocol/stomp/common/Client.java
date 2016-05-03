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

import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.client.message.subscribe.SubscribeHeader.Ack;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.examples.system.client.netty.SystemNettyClientInitializer;


public class Client {

    private AbstractStampyMessageGateway gateway;

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


    public void testSubscription(String id, String dest) throws Exception {
        SubscribeMessage message = new SubscribeMessage(dest, id);
        message.getHeader().setAck(Ack.clientIndividual);
        gateway.broadcastMessage(message);
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


    public void testMessage(String text) throws Exception{
//        sendMessage(StompMessageType.SEND, "Gabrielb");
        String id = "gabrielb";
        SendMessage message = new SendMessage("test", id);
        message.getHeader().setReceipt(id);
        message.getHeader().setDestination("test");
        message.setBody(text);
        getGateway().broadcastMessage(message);
    }

    public void testMIMEtype(String text, String MIMEtype, String charset) throws Exception{
        String id = "gabrielb";
        SendMessage message = new SendMessage("test", id);
        message.getHeader().setReceipt(id);
        message.getHeader().setContentType(MIMEtype);
        String test ="#" + text + "     :     " + "This is a test";
        message.setBody(test);
        getGateway().broadcastMessage(message);
    }


    public void testUnsubscribe(String id) throws InterceptException {
        UnsubscribeMessage message = new UnsubscribeMessage(id);
        getGateway().broadcastMessage(message);
    }
}