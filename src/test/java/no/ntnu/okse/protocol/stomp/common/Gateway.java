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

import asia.stampy.examples.system.server.SystemAcknowledgementHandler;
import asia.stampy.server.netty.Boilerplate;
import asia.stampy.server.netty.ServerNettyMessageGateway;
import no.ntnu.okse.protocol.stomp.listeners.IDontNeedSecurity;
import org.mockito.Mockito;

public class Gateway extends ServerNettyMessageGateway  {
    public static ServerNettyMessageGateway initialize(String host, int port) {
        ServerNettyMessageGateway gateway = Mockito.spy(new Gateway());
        SystemAcknowledgementHandler sys = new SystemAcknowledgementHandler();

        gateway.addMessageListener(new IDontNeedSecurity());

        Boilerplate b = new Boilerplate();
        b.init(gateway, host, port, null, null, sys);

        return gateway;
    }

}
