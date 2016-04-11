package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import asia.stampy.server.netty.ServerNettyMessageGateway;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import no.ntnu.okse.protocol.stomp.STOMPSubscriptionManager;
import no.ntnu.okse.protocol.stomp.common.Gateway;
import org.jboss.netty.channel.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static org.testng.AssertJUnit.assertEquals;

public class DisconnectListenerTest{
    private DisconnectListener listener;
    private DisconnectListener listener_spy;
    private MessageService messageService;
    private MessageService messageService_spy;
    private AbstractStampyMessageGateway gateway_spy;
    private STOMPSubscriptionManager subscritpionManager_spy;

    @BeforeTest
    public void setUp() throws Exception {
        listener = new DisconnectListener();
        messageService = MessageService.getInstance();

        messageService_spy = Mockito.spy(messageService);

        STOMPSubscriptionManager subscriptionManager = new STOMPSubscriptionManager();
        subscriptionManager.initCoreSubscriptionService(SubscriptionService.getInstance());
        subscritpionManager_spy = Mockito.spy(subscriptionManager);

        listener.setSubscriptionManager(subscritpionManager_spy);

        listener_spy = Mockito.spy(listener);
    }

    @AfterTest
    public void tearDown() {
        listener = null;
        listener_spy = null;
    }

    @Test
    public void isForMessage(){
        assertEquals(true, listener_spy.isForMessage(null));
    }

    @Test
    public void getMessageTypes(){
        StompMessageType[] types = listener_spy.getMessageTypes();
        assertEquals(StompMessageType.DISCONNECT, types[0]);
    }

    @Test
    public void messageReceived() throws Exception {
        StampyMessage msg = createSendMessage();
        HostPort hostPort = new HostPort("localhost", 61613);

        listener_spy.messageReceived(msg, hostPort);

        ArgumentCaptor<HostPort> hostPortArgumentCaptor = ArgumentCaptor.forClass(HostPort.class);
        Mockito.verify(listener_spy).cleanUp(hostPortArgumentCaptor.capture());
        Mockito.doNothing().when(listener_spy).cleanUp(hostPort);

        assertEquals( hostPort.getHost(), hostPortArgumentCaptor.getValue().getHost());
        assertEquals( hostPort.getPort(), hostPortArgumentCaptor.getValue().getPort());
        Mockito.reset(listener_spy);
    }

    @Test
    public void forcefulDisconnect(){
        ServerNettyMessageGateway gateway_spy = Mockito.spy(new ServerNettyMessageGateway());

        ArgumentCaptor<SimpleChannelUpstreamHandler> SimpleChannelUpstreamHandlerArgumentcapto= ArgumentCaptor.forClass(SimpleChannelUpstreamHandler.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object handler = invocation.getArguments()[0];
                ChannelHandlerContext ctx = createCTX();
                Channel channel = createChannel();
                InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 1881);
                    HostPort hostPort = new HostPort("127.0.0.1", addr.getPort());

                SimpleChannelUpstreamHandler _handler = (SimpleChannelUpstreamHandler) handler;

                System.out.println("Channel");
                System.out.println(channel);
                Mockito.doReturn(channel).when(ctx).getChannel();
                Mockito.doReturn(addr).when(channel).getRemoteAddress();

                _handler.channelDisconnected(ctx, null);

                ArgumentCaptor<HostPort> hostPortArgumentCaptor = ArgumentCaptor.forClass(HostPort.class);
                Mockito.verify(listener_spy).cleanUp(hostPortArgumentCaptor.capture());
                Mockito.doNothing().when(listener_spy).cleanUp(hostPort);

                assertEquals( hostPort.getHost(), hostPortArgumentCaptor.getValue().getHost());
                assertEquals( hostPort.getPort(), hostPortArgumentCaptor.getValue().getPort());
                Mockito.reset(listener_spy);
                return null;
            }
        }).when(gateway_spy).addHandler(SimpleChannelUpstreamHandlerArgumentcapto.capture());
        listener_spy.setGateway(gateway_spy);

    }

    private Channel createChannel(){
        Channel channel = Mockito.spy(new Channel() {
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
        return channel;
    }
    private ChannelHandlerContext createCTX(){
        ChannelHandlerContext ctx = Mockito.spy(new ChannelHandlerContext() {
            @Override
            public Channel getChannel() {
                return null;
            }

            @Override
            public ChannelPipeline getPipeline() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public ChannelHandler getHandler() {
                return null;
            }

            @Override
            public boolean canHandleUpstream() {
                return false;
            }

            @Override
            public boolean canHandleDownstream() {
                return false;
            }

            @Override
            public void sendUpstream(ChannelEvent channelEvent) {

            }

            @Override
            public void sendDownstream(ChannelEvent channelEvent) {

            }

            @Override
            public Object getAttachment() {
                return null;
            }

            @Override
            public void setAttachment(Object o) {

            }
        });
        return ctx;
    }

    private StampyMessage createSendMessage(){
        DisconnectMessage msg = new DisconnectMessage();
        return msg;
    }

}
