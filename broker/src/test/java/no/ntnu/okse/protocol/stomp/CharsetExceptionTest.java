package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.protocol.stomp.commons.MIMETypeException;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class CharsetExceptionTest {
    @Test
    public void createException(){
        String message = "message";
        MIMETypeException exc = new MIMETypeException(message);
        assertEquals(message, exc.getMessage());
    }
}
