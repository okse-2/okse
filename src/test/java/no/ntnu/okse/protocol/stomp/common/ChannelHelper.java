package no.ntnu.okse.protocol.stomp.common;

import org.jboss.netty.channel.*;
import org.mockito.Mockito;

import java.net.SocketAddress;

public class ChannelHelper {
    public ChannelHelper(){

    }

    public static Channel createChannel(){
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
    public static ChannelHandlerContext createCTX(){
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
}
