package models.dangdang;

import org.apache.commons.httpclient.methods.PostMethod;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 9/25/12
 * Time: 5:13 PM
 */
public interface HttpProxy {
    /**
     * http访问.
     *
     * @param postMethod
     * @return
     * @throws DDAPIInvokeException
     */
    public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException;
}
