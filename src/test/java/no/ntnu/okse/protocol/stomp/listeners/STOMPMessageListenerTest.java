package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class STOMPMessageListenerTest {
    private MessageListener messageListener;
    private MessageListener messageListener_spy;
    private MessageService messageService_spy;

    @BeforeTest
    public void setUp() {
        messageListener = new MessageListener();
        MessageService messageService = MessageService.getInstance();
        STOMPProtocolServer ps = new STOMPProtocolServer("localhost", 61613);

        messageService_spy = Mockito.spy(messageService);

        messageListener.setProtocolServer(ps);
        messageListener.setMessageService(messageService_spy);

        messageListener_spy = Mockito.spy(messageListener);
    }

    @AfterTest
    public void tearDown() {
        messageListener = null;
        messageListener_spy = null;
    }

    @Test
    public void isForMessage(){
        assertEquals(true, messageListener_spy.isForMessage(null));
    }

    @Test
    public void getMessageTypes(){
        StompMessageType[] types = messageListener_spy.getMessageTypes();
        assertEquals(StompMessageType.SEND, types[0]);
    }

    @Test
    public void messageReceived() throws Exception {
        StampyMessage msg = createSendMessage();
        messageListener_spy.messageReceived(msg, new HostPort("localhost", 61613));

        ArgumentCaptor<Message> messageArgument = ArgumentCaptor.forClass(Message.class);

        Mockito.verify(messageService_spy).distributeMessage(messageArgument.capture());
        assertEquals( "Testing", messageArgument.getValue().getMessage());
        assertEquals( "bernt", messageArgument.getValue().getTopic());

        Mockito.reset(messageService_spy);
    }

    @Test
    public void mimeTypes() throws Exception {
        StampyMessage msg = createMimeTypeMessage("plain/text", "utf-8", "test");
        messageListener_spy.messageReceived(msg, new HostPort("localhost", 61613));
    }

    @Test
    public void differentMessageHeaders() throws Exception {
        StampyMessage msg = createSendMessageWithHeaders();
        messageListener_spy.messageReceived(msg, new HostPort("localhost", 61613));

        ArgumentCaptor<Message> messageArgument = ArgumentCaptor.forClass(Message.class);

        Mockito.verify(messageService_spy).distributeMessage(messageArgument.capture());

        assertEquals( "Testing", messageArgument.getValue().getMessage());
        assertEquals( "bernt", messageArgument.getValue().getTopic());

        assertEquals("user defined header", messageArgument.getValue().getAttribute("testing"));
        assertEquals(null, messageArgument.getValue().getAttribute("transaction"));
        assertEquals("2", messageArgument.getValue().getAttribute("content-length"));
        assertEquals("text/plain", messageArgument.getValue().getAttribute("content-type"));
        assertEquals(null, messageArgument.getValue().getAttribute("receipt"));

        Mockito.reset(messageService_spy);
    }

    private StampyMessage createMimeTypeMessage(String mimeType, String encoding, Object body){
        SendMessage msg = new SendMessage();
        msg.setBody(body);
        msg.setMimeType(mimeType, encoding);
        msg.getHeader().setDestination("bernt");
        return msg;
    }

    private StampyMessage createSendMessageWithHeaders(){
        SendMessage msg = new SendMessage();
        msg.setBody("Testing");
        msg.getHeader().setDestination("bernt");
        msg.getHeader().setTransaction("testing");
        msg.getHeader().setContentType("text/plain");
        msg.getHeader().setContentLength(2);
        msg.getHeader().setReceipt("test");
        msg.getHeader().addHeader("testing", "user defined header");
        return msg;
    }

    private StampyMessage createSendMessage(){
        SendMessage msg = new SendMessage();
        msg.setBody("Testing");
        msg.getHeader().setDestination("bernt");
        msg.getHeader().addHeader("testing", "user defined header");
        return msg;
    }

}
