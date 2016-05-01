package no.ntnu.okse.protocol.stomp;

import no.ntnu.okse.protocol.stomp.commons.MIMEType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class MIMETypeTest {
    private MIMEType mime;
    private String charset;
    private String contentType;
    private String mediaType;

    @BeforeMethod
    public void setup(){
        mediaType = "plain/text";
        charset = "utf-8";
        contentType = mediaType + ";charset=" + charset;
        mime = new MIMEType(contentType);
    }

    @AfterMethod
    public void tearDown(){
        mime = null;
    }


    @Test
    public void getCharsetFromContentType(){
        mime = null;
        mime = new MIMEType(null);
        assertEquals(null, mime.getCharset());
        mime = new MIMEType("plain/text");
        assertEquals("utf-8", mime.getCharset());
    }

    @Test
    public void getCharset(){
        assertEquals(charset, mime.getCharset());
    }

    @Test
    public void getMediaType(){
        assertEquals(mediaType, mime.getMediaType());
    }

}
