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

import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.examples.system.server.SystemAcknowledgementHandler;
import asia.stampy.examples.system.server.SystemLoginHandler;
import asia.stampy.server.listener.validate.ServerMessageValidationListener;
import asia.stampy.server.listener.version.VersionListener;
import asia.stampy.server.netty.ServerNettyChannelHandler;
import asia.stampy.server.netty.connect.NettyConnectResponseListener;
import asia.stampy.server.netty.connect.NettyConnectStateListener;
import asia.stampy.server.netty.heartbeat.NettyHeartbeatListener;
import asia.stampy.server.netty.login.NettyLoginMessageListener;
import asia.stampy.server.netty.receipt.NettyReceiptListener;
import asia.stampy.server.netty.subscription.NettyAcknowledgementListenerAndInterceptor;
import asia.stampy.server.netty.transaction.NettyTransactionListener;
import no.ntnu.okse.protocol.stomp.commons.STOMPGateway;
import no.ntnu.okse.protocol.stomp.listeners.IDontNeedSecurity;
import org.mockito.Mockito;

public class Gateway extends STOMPGateway {
    public static STOMPGateway initialize(String host, int port) {
        HeartbeatContainer heartbeatContainer = new HeartbeatContainer();

        STOMPGateway gateway = Mockito.spy(new Gateway());
        gateway.setPort(port);
        gateway.setHost(host);
        gateway.setHeartbeat(1000);
        gateway.setAutoShutdown(true);

        ServerNettyChannelHandler channelHandler = new ServerNettyChannelHandler();
        channelHandler.setGateway(gateway);
        channelHandler.setHeartbeatContainer(heartbeatContainer);

        gateway.addMessageListener(new IDontNeedSecurity());

        gateway.addMessageListener(new ServerMessageValidationListener());

        gateway.addMessageListener(new VersionListener());


        NettyLoginMessageListener login = new NettyLoginMessageListener();
        login.setGateway(gateway);
        login.setLoginHandler(new SystemLoginHandler());
        gateway.addMessageListener(login);

        NettyConnectStateListener connect = new NettyConnectStateListener();
        connect.setGateway(gateway);
        gateway.addMessageListener(connect);

        NettyHeartbeatListener heartbeat = new NettyHeartbeatListener();
        heartbeat.setHeartbeatContainer(heartbeatContainer);
        heartbeat.setGateway(gateway);
        gateway.addMessageListener(heartbeat);

        NettyTransactionListener transaction = new NettyTransactionListener();
        transaction.setGateway(gateway);
        gateway.addMessageListener(transaction);

        SystemAcknowledgementHandler sys = new SystemAcknowledgementHandler();

        NettyAcknowledgementListenerAndInterceptor acknowledgement = new NettyAcknowledgementListenerAndInterceptor();
        acknowledgement.setHandler(sys);
        acknowledgement.setGateway(gateway);
        acknowledgement.setAckTimeoutMillis(200);
        gateway.addMessageListener(acknowledgement);
        gateway.addOutgoingMessageInterceptor(acknowledgement);

        NettyReceiptListener receipt = new NettyReceiptListener();
        receipt.setGateway(gateway);
        gateway.addMessageListener(receipt);

        NettyConnectResponseListener connectResponse = new NettyConnectResponseListener();
        connectResponse.setGateway(gateway);
        gateway.addMessageListener(connectResponse);

        gateway.setHandler(channelHandler);

        return gateway;
    }

}
