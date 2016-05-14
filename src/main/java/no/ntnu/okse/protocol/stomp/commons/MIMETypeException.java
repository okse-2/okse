package no.ntnu.okse.protocol.stomp.commons;

/**
 * Exception that can be thrown when an invalid mimetype is given in a message
 */
public class MIMETypeException extends Exception {
    public MIMETypeException(String msg) {
        super(msg);
    }
}