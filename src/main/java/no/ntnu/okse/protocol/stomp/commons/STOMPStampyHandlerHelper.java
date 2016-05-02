package no.ntnu.okse.protocol.stomp.commons;

import asia.stampy.client.message.ClientMessageHeader;
import asia.stampy.common.gateway.*;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.parsing.UnparseableException;
import asia.stampy.server.message.error.ErrorMessage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class STOMPStampyHandlerHelper extends StampyHandlerHelper {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Handle unexpected error.
     *
     * @param hostPort
     *          the host port
     * @param msg
     *          the msg
     * @param sm
     *          the sm
     * @param e
     *          the e
     */
    public void handleUnexpectedError(HostPort hostPort, String msg, StampyMessage<?> sm, Exception e) {
        try {
            if (sm == null) {
                errorHandle(e, hostPort);
            } else {
                errorHandle(sm, e, hostPort);
            }
        } catch (Exception e1) {
            log.error("Unexpected exception sending error message for " + hostPort, e1);
        }
    }

    /**
     * Handle unparseable message.
     *
     * @param hostPort
     *          the host port
     * @param msg
     *          the msg
     * @param e
     *          the e
     */
    public void handleUnparseableMessage(HostPort hostPort, String msg, UnparseableException e) {
        log.debug("Unparseable message, delegating to unparseable message handler");
        try {
            getUnparseableMessageHandler().unparseableMessage(msg, hostPort);
        } catch (Exception e1) {
            try {
                errorHandle(e1, hostPort);
            } catch (Exception e2) {
                log.error("Could not parse message " + msg + " for " + hostPort, e);
                log.error("Unexpected exception sending error message for " + hostPort, e2);
            }
        }
    }

    /**
     * Error handle. Logs the error.
     *
     * @param message
     *          the message
     * @param e
     *          the e
     * @param hostPort
     *          the host port
     * @throws Exception
     *           the exception
     */
    public void errorHandle(StampyMessage<?> message, Exception e, HostPort hostPort) throws Exception {
        log.error("Handling error, sending error message to " + hostPort, e);
        String receipt = message.getHeader().getHeaderValue(ClientMessageHeader.RECEIPT);
        ErrorMessage error = new ErrorMessage(StringUtils.isEmpty(receipt) ? "n/a" : receipt);
        error.getHeader().setMessageHeader("Could not execute " + message.getMessageType() + " - " + e.getMessage());

        //This was added to the existing handler so that we can call any interceptors when an error message is sent
        getGateway().sendMessage(error, hostPort);
//        getGateway().sendMessage(error.toStompMessage(true), hostPort);
    }

    /**
     * Error handle. Logs the error.
     *
     * @param e
     *          the e
     * @param hostPort
     *          the host port
     * @throws Exception
     *           the exception
     */
    public void errorHandle(Exception e, HostPort hostPort) throws Exception {
        log.error("Handling error, sending error message to " + hostPort, e);
        ErrorMessage error = new ErrorMessage("n/a");
        error.getHeader().setMessageHeader(e.getMessage());

        //This was added to the existing handler so that we can call any interceptors when an error message is sent
        getGateway().sendMessage(error, hostPort);
//        getGateway().sendMessage(error.toStompMessage(true), hostPort);
    }
}
