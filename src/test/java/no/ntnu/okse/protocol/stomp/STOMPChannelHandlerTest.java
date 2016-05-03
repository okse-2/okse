package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import no.ntnu.okse.protocol.stomp.commons.STOMPChannelHandler;
import org.jboss.netty.channel.*;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import static org.testng.AssertJUnit.assertEquals;


public class STOMPChannelHandlerTest {
    private STOMPChannelHandler handler;
    private STOMPProtocolServer ps;
    private String msg;
    private HostPort hostPort;
    private StampyHeartbeatContainer heartbeatContainer;
    private ConcurrentHashMap<HostPort, Channel> sessions;

    @BeforeMethod
    public void setup(){
        msg = "Test";
        hostPort = new HostPort("localhost", 1883);
        handler = Mockito.spy(new STOMPChannelHandler());
        ps = Mockito.spy(new STOMPProtocolServer(hostPort.getHost(), hostPort.getPort()));

        handler.setProtocolServer(ps);
        sessions = new ConcurrentHashMap<>();
        sessions.put(hostPort, createChannel());
        handler.setSessions(sessions);
        heartbeatContainer = new HeartbeatContainer();
        handler.setHeartbeatContainer(heartbeatContainer);
    }

    @AfterMethod
    public void tearDown(){
        hostPort = null;
        msg = null;
        sessions = null;
        heartbeatContainer = null;
        ps = null;
        handler = null;
    }

    @Test
    public void broadcastMessage(){
        Channel channel = sessions.get(hostPort);
        Mockito.doReturn(true).when(channel).isConnected();
        Mockito.doReturn(new InetSocketAddress(1885)).when(channel).getRemoteAddress();
        handler.broadcastMessage(msg);
        Mockito.verify(channel).write(msg);
    }

    @Test
    public void sendMessage(){
        HostPort notConnected = new HostPort("localhost", 1884);
        Channel channel = createChannel();
        sessions.put(notConnected, channel);
        handler.sendMessage(msg, notConnected);
        Mockito.verify(ps).incrementTotalErrors();
    }

    @Test
    public void getHeartbeatContainer(){
        assertEquals(heartbeatContainer, handler.getHeartbeatContainer());
    }

    @Test
    public void getAndSetProtocolServer(){
        assertEquals(ps, handler.getProtocolServer());
        handler.setProtocolServer(null);
        assertEquals(null, handler.getProtocolServer());
    }

    private Channel createChannel(){
        return Mockito.spy(new Channel() {
            @Override
            public int compareTo(Channel o) {
                return 0;
            }

            @Override
            public Integer getId() {
                return null;
            }

            @Override
            public ChannelFactory getFactory() {
                return null;
            }

            @Override
            public Channel getParent() {
                return null;
            }

            @Override
            public ChannelConfig getConfig() {
                return null;
            }

            @Override
            public ChannelPipeline getPipeline() {
                return null;
            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public boolean isBound() {
                return false;
            }

            @Override
            public boolean isConnected() {
                return false;
            }

            @Override
            public SocketAddress getLocalAddress() {
                return null;
            }

            @Override
            public SocketAddress getRemoteAddress() {
                return null;
            }

            @Override
            public ChannelFuture write(Object o) {
                return null;
            }

            @Override
            public ChannelFuture write(Object o, SocketAddress socketAddress) {
                return null;
            }

            @Override
            public ChannelFuture bind(SocketAddress socketAddress) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress socketAddress) {
                return null;
            }

            @Override
            public ChannelFuture disconnect() {
                return null;
            }

            @Override
            public ChannelFuture unbind() {
                return null;
            }

            @Override
            public ChannelFuture close() {
                return null;
            }

            @Override
            public ChannelFuture getCloseFuture() {
                return null;
            }

            @Override
            public int getInterestOps() {
                return 0;
            }

            @Override
            public boolean isReadable() {
                return false;
            }

            @Override
            public boolean isWritable() {
                return false;
            }

            @Override
            public ChannelFuture setInterestOps(int i) {
                return null;
            }

            @Override
            public ChannelFuture setReadable(boolean b) {
                return null;
            }

            @Override
            public Object getAttachment() {
                return null;
            }

            @Override
            public void setAttachment(Object o) {

            }
        });
    }
}
