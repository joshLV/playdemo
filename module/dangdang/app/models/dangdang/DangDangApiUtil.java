package models.dangdang;


import models.order.ECoupon;
import models.sales.Goods;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 当当API工具类.
 * <p/>
 * User: sujie
 * Date: 9/13/12
 * Time: 2:02 PM
 */
public class DangDangApiUtil {
    private static final String SPID = "1";
    private static final String MD5 = "MD5";
    private static final String VER = "1.0";
    private static final String XML = "XML";
    private static final String SECRET_KEY = "";
    private static final String SIGN_METHOD = "1";


    public static final String SYNC_URL = "http://tuanapi.dangdang.com/team_inter_api/public/push_team_stock.php";

    /**
     * 返回一百券系统中商品总销量.
     * 调用当当的API
     *
     * @return
     */
    public static void syncSellCount(Goods goods) {
        int sellCount = goods.saleCount;

        String data = String.format("<data><row><spgid><![CDATA[%s]]></spgid><sellcount><![CDATA[%s]]></sellcount" +
                "></row></data>", goods.id, sellCount);
        Response response = DangDangApiUtil.access(SYNC_URL, data, "push_team_stock");
        //todo 返回结果处理
    }

    /**
     * 查询当前券是否已在当当上退款了.
     *
     * @param eCoupon
     * @return
     */
    public static boolean isRefund(ECoupon eCoupon) {
        //todo
        return false;
    }

    /**
     * 通知当当当前的券已经使用.
     *
     * @param eCoupon
     */
    public static void notifyVerified(ECoupon eCoupon) {
        //todo
    }

    /**
     * 发送券号短信.
     *
     * @param data xml格式
     *
     */
    public static void sendSMS(String data) {

    }

    /**
     * 发送http请求，并返回xml
     *
     * @param url
     * @param data
     * @param apiName
     * @return
     */
    public static Response access(String url, String data, String apiName) {
        //构造HttpClient的实例
        HttpClient httpClient = new HttpClient();
        //创建GET方法的实例
        PostMethod postMethod = new PostMethod(url);
        //将表单的值放入postMethod中
        postMethod.addParameter("spid", SPID);
        postMethod.addParameter("result_format", XML);
        postMethod.addParameter("ver", VER);
        postMethod.addParameter("sign_method", SIGN_METHOD);
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        postMethod.addParameter("call_time", time);
        postMethod.addParameter("data", data);
        String sign = getSign(data, time, apiName);
        postMethod.addParameter("sign", sign);
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

    private static String getSign(String data, String time, String apiName) {
        byte[] result;
        String tt = " ";
        try {
            MessageDigest alg = java.security.MessageDigest.getInstance(MD5);
            alg.update((SPID + apiName + VER + data + SECRET_KEY + time).getBytes());
            result = alg.digest();
            for (int i = 0; i < result.length; i++) {
                tt += (char) result[i];
            }

            return tt;

        } catch (Exception ex) {
            return null;
        }

    }

/*
    public static String inputStream2String(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = -1;
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        return baos.toString();
    }*/
}
