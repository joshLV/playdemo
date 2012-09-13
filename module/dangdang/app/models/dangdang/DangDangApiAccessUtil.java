package models.dangdang;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 9/13/12
 * Time: 2:02 PM
 */
public class DangDangApiAccessUtil {
    public Response access(String url, Map<String, String> params) {
        //构造HttpClient的实例  
        HttpClient httpClient = new HttpClient();
        //创建GET方法的实例  
        PostMethod postMethod = new PostMethod(url);
        //填入各个表单域的值
        NameValuePair[] postData = new NameValuePair[params.keySet().size()];
        Iterator it = params.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            postData[i] = new NameValuePair(key.toString(), value.toString());
            i++;
        }
        //将表单的值放入postMethod中 u
        postMethod.setRequestBody(postData);
        try {
            //执行postMethod  
            int statusCode = httpClient.executeMethod(postMethod); // HttpClient对于要求接受后继服务的请求，象POST和PUT等不能自动处理转发  
            // 301或者302  
            if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                //从头中取出转向的地址  
                return new Response(postMethod.getResponseBodyAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String inputStream2String(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = -1;
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        return baos.toString();
    }
}
