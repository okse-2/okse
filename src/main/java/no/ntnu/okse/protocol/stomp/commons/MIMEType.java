package no.ntnu.okse.protocol.stomp.commons;

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