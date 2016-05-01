package no.ntnu.okse.protocol.stomp.commons;

import asia.stampy.client.message.send.SendMessage;
import asia.stampy.common.message.StampyMessage;

/**
 * Created by Ogdans3 on 30.04.2016.
 */
public class MIMEType {
    private String contentType;
    private String charset;
    private String mediaType;

    public MIMEType(String contentType){
        this.contentType = contentType;
        if(contentType == null){
            this.mediaType = null;
            this.charset = null;
        }else{
            this.mediaType = contentType.split(";")[0];
            this.charset = getCharsetFromContentType(contentType);
        }
    }

    private String getCharsetFromContentType(String contentType){
        //Assume that it is binary data and that it should be treated as utf-8
        //According to the specification of STOMP the server should assume any content to be
        //interpreted as utf-8 binary blob if a contentType is not given.
        if(contentType == null)
            return "utf-8";
        String[] values = contentType.split(";");
        for (String value : values) {
            value = value.trim();

            if (value.toLowerCase().startsWith("charset=")) {
                return value.substring("charset=".length());
            }
        }
        return "utf-8";
    }

    public String getCharset() {
        return charset;
    }

    public String getMediaType(){
        return mediaType;
    }
}
