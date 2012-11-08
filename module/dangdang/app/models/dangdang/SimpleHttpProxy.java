package models.dangdang;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * 简单的http访问代理.
 * <p/>
 * User: sujie
 * Date: 9/25/12
 * Time: 5:17 PM
 */
public class SimpleHttpProxy implements HttpProxy {

    /**
     * http访问.
     *
     * @param postMethod
     * @return
     * @throws DDAPIInvokeException
     */
    public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
        
        HttpClient httpClient = null;
        try {
            //构造HttpClient的实例
            httpClient = new HttpClient();
            //执行postMethod
            int statusCode = httpClient.executeMethod(postMethod); // HttpClient对于要求接受后继服务的请求，象POST和PUT等不能自动处理转发
            // 200
            if (statusCode == HttpStatus.SC_OK) {
                //从头中取出转向的地址
                return new Response(postMethod.getResponseBodyAsStream());
            }
        } catch (Exception e) {
            throw new DDAPIInvokeException(e.getMessage());
        }
        return new Response();
    }

}
