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
