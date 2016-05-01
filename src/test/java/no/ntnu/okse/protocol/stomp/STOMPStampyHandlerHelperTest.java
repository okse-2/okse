package no.ntnu.okse.protocol.stomp;

import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.DefaultUnparseableMessageHandler;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.UnparseableMessageHandler;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.common.parsing.UnparseableException;
import asia.stampy.server.message.error.ErrorMessage;
import no.ntnu.okse.protocol.stomp.common.Gateway;
import no.ntnu.okse.protocol.stomp.commons.STOMPChannelHandler;
import no.ntnu.okse.protocol.stomp.commons.STOMPStampyHandlerHelper;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class STOMPStampyHandlerHelperTest {
    private STOMPStampyHandlerHelper handler_spy;
    private HostPort hostPort;
    private String msg;
    private Exception e;
    private UnparseableException unparsableException;
    private StampyMessage stampyMessage;
    private AbstractStampyMessageGateway gateway;
    private UnparseableMessageHandler unparsableHandler;

    @BeforeMethod
    public void setup(){
        hostPort = new HostPort("localhost", 1883);
        msg = "Test";
        e = new Exception("Test");
        unparsableException = new UnparseableException("Testing unparsable exception");
        stampyMessage = createStampyMessage();

        gateway = Gateway.initialize(hostPort.getHost(), hostPort.getPort());
        unparsableHandler = Mockito.spy(new DefaultUnparseableMessageHandler());
        handler_spy = Mockito.spy(new STOMPStampyHandlerHelper());
        handler_spy.setGateway(gateway);
    }

    private StampyMessage createStampyMessage() {
        ErrorMessage msg = new ErrorMessage("Test error handling");
        return (StampyMessage)msg;
    }

    @AfterMethod
    public void tearDown(){
        unparsableHandler = null;
        gateway = null;
        hostPort = null;
        msg = null;
        e = null;
        unparsableException = null;

        handler_spy = null;
    }

    @Test
    public void handleUnexpectedError() throws InterceptException {
        handler_spy.handleUnexpectedError(hostPort, msg, null, e);

        ArgumentCaptor<HostPort> hpArguments = ArgumentCaptor.forClass(HostPort.class);
        ArgumentCaptor<StampyMessage> stampyMessageArguments = ArgumentCaptor.forClass(StampyMessage.class);
        Mockito.verify(gateway).sendMessage(stampyMessageArguments.capture(), hpArguments.capture());
    }
    @Test
    public void handleUnexpectedErrorWithStampyMessage() throws InterceptException {
        handler_spy.handleUnexpectedError(hostPort, msg, stampyMessage, e);
        ArgumentCaptor<HostPort> hpArguments = ArgumentCaptor.forClass(HostPort.class);
        ArgumentCaptor<StampyMessage> stampyMessageArguments = ArgumentCaptor.forClass(StampyMessage.class);
        Mockito.verify(gateway).sendMessage(stampyMessageArguments.capture(), hpArguments.capture());
    }

    @Test
    public void handleUnexpectedErrorException() throws Exception {
        Mockito.doThrow(new Exception("Testing exception in handleUnexcpectedErrorException"))
                .when(handler_spy).errorHandle(e, hostPort);
        Mockito.doThrow(new Exception("Testing exception in handleUnexcpectedErrorException"))
                .when(handler_spy).errorHandle(stampyMessage, e, hostPort);
        handler_spy.handleUnexpectedError(hostPort, msg, null, e);
        handler_spy.handleUnexpectedError(hostPort, msg, stampyMessage, e);
    }

    @Test
    public void handleUnparseableMessage(){
        handler_spy.handleUnparseableMessage(hostPort, msg, unparsableException);
    }

    @Test
    public void handleUnparseableMessageException() throws Exception {
        handler_spy.setUnparseableMessageHandler(unparsableHandler);

        Mockito.doThrow(new Exception("Testing unparsable exception")).when(unparsableHandler).unparseableMessage(msg, hostPort);
        handler_spy.handleUnparseableMessage(hostPort, msg, unparsableException);
    }

    @Test
    public void handleUnparseableMessageExceptionOnSecondCatch() throws Exception {
        handler_spy.setUnparseableMessageHandler(unparsableHandler);
        Exception exc = new Exception("Testing unparsable exception");
        Mockito.doThrow(exc).when(unparsableHandler).unparseableMessage(msg, hostPort);
        Mockito.doThrow(new Exception("Testing unparsable exception, second catch")).when(handler_spy).errorHandle(exc, hostPort);
        handler_spy.handleUnparseableMessage(hostPort, msg, unparsableException);
    }

}
