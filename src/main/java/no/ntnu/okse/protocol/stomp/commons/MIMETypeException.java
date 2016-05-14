package no.ntnu.okse.protocol.stomp.commons;

/**
 * Exception that can be thrown when an invalid charset is given
 */
public class MIMETypeException extends Exception {
    public MIMETypeException(String msg) {
        super(msg);
    }
}