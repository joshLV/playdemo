package functional;

import com.ning.http.multipart.FilePart;
import com.ning.http.multipart.MultipartRequestEntity;
import com.ning.http.multipart.Part;
import com.ning.http.multipart.StringPart;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.order.*;
import models.resale.Resaler;
import models.taobao.TaobaoCouponUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.mvc.Http.Request;
import play.mvc.Http.Response;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author likang
 *         Date: 12-11-30
 */
public class TaobaoCouponAPITest extends FunctionalTest {
    OuterOrder outerOrder = null;
Resaler resaler;
    private final long TAOBAO_ORDER_ID = 259599322131179L;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        resaler=FactoryBoy.lastOrCreate(Resaler.class);
        resaler.loginName=Resaler.TAOBAO_LOGIN_NAME;
        resaler.taobaoSellerId = 828005208L;
        resaler.taobaoCouponServiceKey = "abc";
        resaler.partner="TB";
        resaler.save();
        outerOrder = FactoryBoy.create(OuterOrder.class, new BuildCallback<OuterOrder>() {
            @Override
            public void build(OuterOrder target) {
                target.partner = OuterOrderPartner.TB;
                target.resaler = resaler;
                target.orderId = String.valueOf(TAOBAO_ORDER_ID);
            }
        });
        FactoryBoy.create(ECoupon.class);
        FactoryBoy.create(ECoupon.class);
    }

    @Test
    public void testSign() {
        resaler.taobaoSellerId=11L;
        resaler.save();
        Map<String, String> params = prepareParams();
        params.put("sign", "1");
        Http.Response response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":501}", response);
    }

    @Test
    public void testParams() {
        Map<String, String> params = prepareParams();
        params.remove("order_id");
        resign(params);

        params.put("sign", "1");
        Http.Response response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":502}", response);
    }

    @Test
    public void testSend() {
        long outerOrderSize = OuterOrder.count();

        Map<String, String> params = prepareParams();
        params.put("order_id", "321111");
        params.put("seller_nick", "券生活8");
        resign(params);
        Http.Response response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":200}", response);
        assertEquals(outerOrderSize + 1, OuterOrder.count());
    }

    @Test
    public void testResend() {
        assertEquals(OuterOrderStatus.ORDER_COPY, outerOrder.status);
        Map<String, String> params = prepareParams();
        params.put("method", "resend");
        resign(params);

        Http.Response response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":200}", response);

        outerOrder.refresh();
        assertEquals(OuterOrderStatus.RESEND_COPY, outerOrder.status);


        params.put("order_id", "123321");
        resign(params);
        response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":504}", response);
    }

    @Test
    public void testCancel() {
        assertEquals(OuterOrderStatus.ORDER_COPY, outerOrder.status);
        Map<String, String> params = prepareParams();
        params.put("method", "cancel");
        params.put("cancel_num", "1");
        resign(params);

        Http.Response response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":200}", response);

        outerOrder.refresh();
        assertEquals(OuterOrderStatus.REFUND_COPY, outerOrder.status);

        params.put("order_id", "123321");
        resign(params);
        response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":504}", response);

    }

    @Test
    public void testMobileModify() {

        Map<String, String> params = prepareParams();
        params.put("method", "modified");
        params.put("mobile", "13472581854");
        resign(params);

        Http.Response response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":200}", response);

        OrderItems orderItem = OrderItems.find("byOrder", outerOrder.ybqOrder).first();
        orderItem.refresh();

        assertEquals("13472581854", orderItem.phone);

        params.put("order_id", "123321");
        resign(params);
        response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":504}", response);
    }

    @Test
    public void testOrderModify() {
        List<ECoupon> couponList = ECoupon.find("byOrder", outerOrder.ybqOrder).fetch();
        assertTrue(couponList.size() > 0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = new Date();
        Date end = new Date(System.currentTimeMillis() + 60000 * 60);

        Map<String, String> params = prepareParams();
        params.put("method", "order_modify");
        params.put("sub_method", "1");
        params.put("data", "{\"valid_start\":\"" + dateFormat.format(start) + "\", \"valid_ends\":\"" + dateFormat.format(end) + "\"}");
        resign(params);

        Http.Response response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":200}", response);

        for (ECoupon coupon : couponList) {
            coupon.refresh();
            assertEquals(dateFormat.format(coupon.effectiveAt), dateFormat.format(start));
            assertEquals(dateFormat.format(coupon.expireAt), dateFormat.format(end));
        }

        //测试找不倒订单
        params.put("order_id", "123321");
        resign(params);
        response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":504}", response);

        //测试无效的data
        params.put("order_id", String.valueOf(TAOBAO_ORDER_ID));
        params.put("data", "\"valid_start\":\"" + dateFormat.format(start) + "\", \"valid_ends\":\"" + dateFormat.format(end) + "\"}");
        resign(params);

        response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":505}", response);

        //测试submethod
        params.put("data", "{\"valid_start\":\"" + dateFormat.format(start) + "\", \"valid_ends\":\"" + dateFormat.format(end) + "\"}");
        params.put("sub_method", "3");
        resign(params);

        response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":506}", response);

        //测试无效的日期格式
        params.put("data", "{\"valid_start\":\"a" + dateFormat.format(start) + "\", \"valid_ends\":\"" + dateFormat.format(end) + "\"}");
        params.put("sub_method", "1");
        resign(params);

        response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":507}", response);

        params.put("data", "{\"valid_start\":\"" + dateFormat.format(start) + "\", \"valid_ends\":\"a" + dateFormat.format(end) + "\"}");
        params.put("sub_method", "1");
        resign(params);

        response = POST2("/api/v1/taobao/coupon", params);

        assertIsOk(response);
        assertContentEquals("{\"code\":508}", response);
    }


    private void resign(Map<String, String> params) {
        params.remove("sign");
        params.put("sign", TaobaoCouponUtil.sign(resaler.taobaoCouponServiceKey, params));
    }

    private Map<String, String> prepareParams() {
        Map<String, String> params = new HashMap<>();
        params.put("valid_ends", "2013-01-11 23:59:59");
        params.put("outer_iid", "32");
        params.put("item_title", "测试用 拍下概不负责");
        params.put("taobao_sid", "828005208");
        params.put("order_id", String.valueOf(TAOBAO_ORDER_ID));
        params.put("send_type", "2");
        params.put("timestamp", "2012-11-30 17:16:01");
        params.put("sign", "8D5F91B2FC6D2E8843461AB1D8F605B1");
        params.put("num", "1");
        params.put("consume_type", "0");
        params.put("valid_start", "2012-11-30 00:00:00");
        params.put("token", "a6670b68da9bdb8c6365c49b71e395b7");
        params.put("method", "send");
        params.put("num_iid", "21339404852");
        params.put("sms_template", "验证码$code.您已成功订购券生活8提供的测试用 拍下概不负责,有效期2012/11/30至2013/01/11,消费时请出示本短信以验证.淘宝客服电话057188158198.");
        params.put("seller_nick", "券生活8");
        params.put("mobile", "13472581853");

        return params;
    }

    /**
     * 解决目前使用的play 版本，测试时无法post中文的问题
     */
    public static Response POST2(Object url, Map<String, String> parameters) {
        return POST(newRequest(), url, parameters, new HashMap<String, File>());
    }

    public static Response POST(Request request, Object url, Map<String, String> parameters, Map<String, File> files) {
        List<Part> parts = new ArrayList<Part>();

        for (String key : parameters.keySet()) {
            final StringPart stringPart = new StringPart(key, parameters.get(key), request.encoding);
            parts.add(stringPart);
        }

        for (String key : files.keySet()) {
            Part filePart;
            try {
                filePart = new FilePart(key, files.get(key));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            parts.add(filePart);
        }

        MultipartRequestEntity requestEntity = new MultipartRequestEntity(parts.toArray(new Part[]{}), null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            requestEntity.writeRequest(baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InputStream body = new ByteArrayInputStream(baos.toByteArray());
        String contentType = requestEntity.getContentType();
        Http.Header header = new Http.Header();
        header.name = "content-type";
        header.values = Arrays.asList(new String[]{contentType});
        request.headers.put("content-type", header);
        return POST(request, url, MULTIPART_FORM_DATA, body);
    }


}
