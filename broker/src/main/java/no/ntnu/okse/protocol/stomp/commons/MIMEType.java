package no.ntnu.okse.protocol.stomp.commons;

public class MIMEType {
    private String contentType;
    private String charset;
    private String mediaType;

    /**
     * Sets the content type, media type and charset.
     * @param contentType
     */
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

    /**
     * Seperates the charset from the content type
     * The default value of the charset is utf-8, this was done according to the STOMP specification
     * @param contentType the content type to get the charset from
     * @return the charset
     */
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

    /**
     * Gets the charset
     * @return the charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Gets the media type
     * @return the media type
     */
    public String getMediaType(){
        return mediaType;
    }

    /**
     * This method validates the mime type and charset
     * @return true if the mime type and charset is valid
     */
    public boolean isValid() {
        if(charset == null)
            return true;
        if(charset.equals(""))
            return false;
        return true;
    }
}
