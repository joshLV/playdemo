package models.dangdang;

import java.io.Serializable;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 9/13/12
 * Time: 2:27 PM
 */
public class Response implements Serializable {

    private String statusCode;
    private String errorCode;
    private String desc;
    private String spid;
    private String ver;


    public Response() {

    }

    public Response(String responseBodyAsString) {

    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDesc() {
        return desc;
    }

    public String getSpid() {
        return spid;
    }

    public String getVer() {
        return ver;
    }
}
