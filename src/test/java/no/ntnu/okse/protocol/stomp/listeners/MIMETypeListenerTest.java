package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.protocol.stomp.commons.MIMETypeException;
import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class MIMETypeListenerTest {
    private MIMEtypeListener listener;
    private MIMEtypeListener listener_spy;
    private AbstractStampyMessageGateway gateway_spy;

    @BeforeTest
    public void setUp() throws Exception {
        listener = new MIMEtypeListener();
        listener_spy = Mockito.spy(listener);
    }

    @AfterTest
    public void tearDown() throws Exception {
        if(gateway_spy != null)
            gateway_spy.shutdown();
        gateway_spy = null;
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
        assertEquals(StompMessageType.SEND, types[0]);
    }

    @Test
    public void messageReceived() throws Exception {
        HostPort hostPort = new HostPort("localhost", 61613);

        StampyMessage msg = createSendMessage(false);
        try{
            listener.messageReceived(msg, hostPort);
            assertEquals(true, false);
        }catch(MIMETypeException e){
            assertEquals(true, true);
        }

        StampyMessage validMsg = createSendMessage(true);
        try{
            listener.messageReceived(validMsg, hostPort);
            assertEquals(true, true);
        }catch(MIMETypeException e){
            assertEquals(true, false);
        }

        StampyMessage nullCharset = createSendMessageNullCharset();
        try{
            listener.messageReceived(nullCharset, hostPort);
            assertEquals(true, true);
        }catch(MIMETypeException e){
            assertEquals(true, false);
        }
    }

    private StampyMessage createSendMessageNullCharset() {
        return new SendMessage();
    }

    private StampyMessage createSendMessage(boolean valid){
        SendMessage msg = new SendMessage();
        if(valid)
            msg.getHeader().setContentType("plain/text;charset=utf-8");
        else
            msg.getHeader().setContentType("plain/text;charset=");
        return msg;
    }
}
