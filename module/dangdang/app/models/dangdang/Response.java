package models.dangdang;

import org.dom4j.Element;

import java.io.Serializable;

/**
 * <p/>
 * User: sujie
 * Date: 9/13/12
 * Time: 2:27 PM
 */
public class Response implements Serializable {

    public String statusCode;
    public int errorCode;
    public String desc;
    public String spid;
    public String ver;
    public String consumeId;
    public Long ddOrderId;
    public String ybqOrderId;

    public Response() {

    }

    /**
     *
     * <resultObject>
     * <status_code>-1</status_code>
     * <error_code>1002</error_code>
     * <desc>
     * <![CDATA[ 验证失败 ]]>
     * </desc>
     * <spid>1</spid>
     *  <ver>1.0</ver>
     * </resultObject>
     * @param responseBodyAsString  xml字符串
     */
    public Response(String responseBodyAsString) {
        Parser<Response> parser = new Parser<Response>() {
            @Override
            public Response parse(Element node) {


            }
        }

    }
}
