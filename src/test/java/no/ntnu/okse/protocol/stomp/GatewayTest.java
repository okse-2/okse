package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.protocol.stomp.common.Gateway;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class GatewayTest {
    private STOMPGateway gateway;
    private String host = "localhost";
    private int port = 61631;

    @BeforeMethod
    public void setup() throws Exception {
        gateway = Gateway.initialize(host, port);
        gateway.setHost("localhost");
        gateway.setPort(61631);
        gateway.connect();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        gateway.shutdown();
        gateway = null;
    }

    @Test
    public void connect() throws Exception {
        assertEquals(true, gateway.getServer().isBound());
        gateway.shutdown();
        //Test that it does not crash if we try to connect twice
        gateway.connect();
        gateway.connect();

        //Test that it does not crash if the server object is present, but not bound to an address
        gateway.getServer().unbind();
        gateway.connect();
    }

    @Test
    public void getServer() throws Exception {
        assertNotNull(gateway.getServer());
    }

    @Test
    public void setHost(){
        gateway.setHost(host);
        assertEquals(host, gateway.getHost());
    }

    @Test
    public void shutdown() throws Exception {
        gateway.shutdown();
        assertEquals(null, gateway.getServer());
        //Test that it does not crash if we try to shutdown twice
        gateway.shutdown();
    }
}
