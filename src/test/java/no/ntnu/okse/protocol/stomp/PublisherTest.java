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

import no.ntnu.okse.protocol.stomp.common.Client;

public class PublisherTest {

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.init(null);
        client.testConnect(String.valueOf(0 + (int)(Math.random() * 500)));

        while(true){
            System.out.println("Send message");
            client.testMessage();
            Thread.sleep(3000);
        }
    }
}