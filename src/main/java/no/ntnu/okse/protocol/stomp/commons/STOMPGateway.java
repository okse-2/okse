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
package no.ntnu.okse.protocol.stomp.commons;

import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import asia.stampy.server.netty.ServerNettyMessageGateway;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class actually creates and returns the gateway object
 *
 * This class was extended from ServerNettyMessageGateway to
 * fix some issues with the shutdown of the server.
 * Aswell as adding the option to specifically set the host
 * of the STOMP server.
 *
 * We decided that this was much easier than to fork the entire project
 *
 * @see ServerNettyMessageGateway for any changes modifications
 */
public class STOMPGateway extends ServerNettyMessageGateway {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private NioServerSocketChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());

    private Channel server;
    private String host;

    /**
     * Inits the server
     * @return ServerBootstrap instance
     * @see ServerBootstrap
     */
    private ServerBootstrap init() {
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        initializeChannel(bootstrap);

        return bootstrap;
    }

    /**
     * Returns the server, used in testing and debugging
     * @return
     */
    public Channel getServer(){
        return server;
    }

    /**
     * Connects the server, it binds the server
     * @throws Exception
     */
    @Override
    public void connect() throws Exception {
        if (server == null) {
            ServerBootstrap bootstrap = init();
            server = bootstrap.bind(new InetSocketAddress(getHost(), getPort()));
            log.info("Bound to {}", getHost() + ":" + getPort());
            log.info(String.valueOf(server.getLocalAddress()), String.valueOf(server.getRemoteAddress()));
        } else if (server.isBound()) {
            log.warn("Already bound");
        } else {
            log.error("Acceptor in unrecognized state: isBound {}, isConnected {}, ", server.isBound(), server.isConnected());
        }
    }

    /**
     * Sets the host for the gateway
     * @param host the host
     */
    public void setHost(String host){
        this.host = host;
    }

    /**
     * Gets the host
     * @return the host for the gateway
     */
    public String getHost(){
        return this.host;
    }

    /**
     * shuts down the server
     * If the server is null it logs an exception
     * @throws Exception
     * @see asia.stampy.common.gateway.AbstractStampyMessageGateway#shutdown()
     */
    @Override
    public void shutdown() throws Exception {
        if (server == null){
            log.error("Server was null, cannot shutdown!");
            return;
        }
        ChannelFuture cf = server.close();
        cf.awaitUninterruptibly();
        server = null;
        log.info("Server has been shut down");
    }

}
