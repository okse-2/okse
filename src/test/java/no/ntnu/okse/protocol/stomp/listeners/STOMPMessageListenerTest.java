package no.ntnu.okse.protocol.stomp.listeners;

import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.core.messaging.MessageService;
import no.ntnu.okse.core.subscription.SubscriptionService;
import no.ntnu.okse.protocol.stomp.STOMPProtocolServer;
import no.ntnu.okse.protocol.stomp.listeners.MessageListener;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class STOMPMessageListenerTest {
    private MessageListener messageListener;
    private STOMPProtocolServer ps;
    private MessageService messageService;
    private MessageListener messageListener_spy;
    private MessageService messageService_spy;

    @BeforeTest
    public void setUp() {
        messageListener = new MessageListener();
        messageService = MessageService.getInstance();
        ps = new STOMPProtocolServer("localhost", 61613);

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
    }

    private StampyMessage createSendMessage(){
        SendMessage msg = new SendMessage();
        msg.setBody("Testing");
        msg.getHeader().setDestination("bernt");
        return msg;
    }

}
