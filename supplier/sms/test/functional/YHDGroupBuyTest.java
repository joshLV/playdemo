package functional;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import models.accounts.Account;
import models.accounts.AccountCreditable;
import models.accounts.util.AccountUtil;
import models.order.*;
import models.resale.Resaler;
import models.resale.ResalerCreditable;
import models.sales.Goods;
import models.yihaodian.YHDUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

/**
 * @author likang
 *         Date: 12-9-26
 */
public class YHDGroupBuyTest extends FunctionalTest{
    public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    @Before
    public void setup(){
        FactoryBoy.deleteAll();
        FactoryBoy.create(Goods.class, "Electronic");
        Resaler resaler = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.YHD_LOGIN_NAME;
                target.creditable = ResalerCreditable.YES;
            }
        });
        Account account= AccountUtil.getResalerAccount(resaler.getId());
        account.creditable = AccountCreditable.YES;
        account.save();

    }
    @Test
    public void testInform(){
        TreeMap<String, String> params = new TreeMap<>();

        /// =========================  测试参数
        Http.Response response = POST("/api/v1/yhd/gb/order-inform", params);
        errorCount(10, response);

        params = orderInformParams();

        /// =========================   测试 sign
        params.put("sign", "testsign");
        response = POST("/api/v1/yhd/gb/order-inform", params);
        try{
            JSONObject jsonObject = new JSONObject(response.out.toString());
            assertEquals(1, jsonObject.getJSONObject("response").getInt("errorCount"));
            assertEquals("sign不匹配",jsonObject.getJSONObject("response")
                    .getJSONObject("errInfoList")
                    .getJSONArray("errDetailInfo")
                    .getJSONObject(0)
                    .getString("errorDes"));
        }catch (JSONException e){
            fail();
        }

        // 换回正常的 sign
        resign(params);


        Goods goods = FactoryBoy.last(Goods.class);
        // 测试参数
        //数量
        params.put("productNum", "0");
        params.put("orderAmount", goods.salePrice.multiply(new BigDecimal("0")).toString());
        resign(params);
        response = POST("/api/v1/yhd/gb/order-inform", params);
        errorCount(1, response);
        params.put("productNum", "2");

        //价格
        params.put("productPrice", "-1");
        params.put("orderAmount", new BigDecimal("-1").multiply(new BigDecimal("2")).toString());
        resign(params);
        response = POST("/api/v1/yhd/gb/order-inform", params);
        errorCount(1, response);
        params.put("productPrice", goods.salePrice.toString());

        //总价
        params.put("orderAmount", "1");
        resign(params);
        response = POST("/api/v1/yhd/gb/order-inform", params);
        errorCount(1, response);
        params.put("orderAmount", goods.salePrice.multiply(new BigDecimal("2")).toString());

        //手机
        params.put("userPhone", "");
        resign(params);
        response = POST("/api/v1/yhd/gb/order-inform", params);
        errorCount(1, response);

        params.put("userPhone", "13472581853");
        resign(params);
        // =========================  测试生成订单
        response = POST("/api/v1/yhd/gb/order-inform", params);
        try{
            JSONObject jsonObject = new JSONObject(response.out.toString()).getJSONObject("response");
            assertEquals(0, jsonObject.getInt("errorCount"));
            assertEquals(1, jsonObject.getInt("updateCount"));
        }catch (JSONException e){
            fail();
        }
        assertEquals(1, Order.findAll().size());
        assertEquals(1, OrderItems.findAll().size());
        assertEquals(2, ECoupon.findAll().size());

        // ========================= 测试重复订单
        response = POST("/api/v1/yhd/gb/order-inform", params);
        errorCount(1, response);
    }

    /**
     * 测试处理一号店的查询消费券请求
     */
    @Test
    public void testVouchersGet(){
        final Order ybqOrder = FactoryBoy.create(Order.class);
        final OuterOrder outerOrder = FactoryBoy.create(OuterOrder.class, new BuildCallback<OuterOrder>() {
            @Override
            public void build(OuterOrder target) {
                target.partner = OuterOrderPartner.YHD;
                target.ybqOrder = ybqOrder;
            }
        });
        TreeMap<String, String> params = new TreeMap<>();
        params.put("orderCode", String.valueOf(outerOrder.orderId));
        params.put("partnerOrderCode", outerOrder.ybqOrder.orderNumber);
        params.put("sign", "testsign");

        //测试签名
        Http.Response response = POST("/api/v1/yhd/gb/vouchers-get", params);
        errorCount(1, response);
        resign(params);

        //测试外部订单号
        params.put("orderCode", "testcode");
        resign(params);
        response = POST("/api/v1/yhd/gb/vouchers-get", params);
        errorCount(1, response);
        params.put("orderCode", String.valueOf(outerOrder.orderId));

        //测试内部订单号
        params.put("partnerOrderCode", "testcode");
        resign(params);
        response = POST("/api/v1/yhd/gb/vouchers-get", params);
        errorCount(1, response);
        params.put("partnerOrderCode", outerOrder.ybqOrder.orderNumber);

        resign(params);
        response = POST("/api/v1/yhd/gb/vouchers-get", params);
        assertIsOk(response);
    }

    @Test
    public void testVoucherResend(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        //生成订单
        TreeMap<String ,String> params = orderInformParams();
        resign(params);
        Http.Response response = POST("/api/v1/yhd/gb/order-inform", params);
        errorCount(0, response);

        params = new TreeMap<>();

        Order order = (Order)Order.findAll().get(0);
        OuterOrder outerOrder = (OuterOrder)OuterOrder.findAll().get(0);
        ECoupon coupon = (ECoupon)ECoupon.findAll().get(0);
        params.put("orderCode", String.valueOf(outerOrder.orderId));
        params.put("partnerOrderCode", order.orderNumber);
        params.put("voucherCode", coupon.eCouponSn);
        params.put("receiveMobile", "13472581853");
        params.put("requestTime", dateFormat.format(new Date()));
        resign(params);
        response = POST("/api/v1/yhd/gb/voucher-resend", params);
        errorCount(0, response);

        //测试外部订单号
        params.put("orderCode", "testcode");
        resign(params);
        response = POST("/api/v1/yhd/gb/voucher-resend", params);
        errorCount(1, response);
        params.put("orderCode", String.valueOf(outerOrder.orderId));

        //测试内部订单号
        params.put("partnerOrderCode", "testcode");
        resign(params);
        response = POST("/api/v1/yhd/gb/voucher-resend", params);
        errorCount(1, response);
        params.put("partnerOrderCode", outerOrder.ybqOrder.orderNumber);

        //测试请求时间
        params.put("requestTime", dateFormat.format(new Date(System.currentTimeMillis() - 1000000)));
        resign(params);
        response = POST("/api/v1/yhd/gb/voucher-resend", params);
        errorCount(1, response);
        params.put("requestTime", dateFormat.format(new Date()));

        //测试券不存在
        params.put("voucherCode", "testcode");
        resign(params);
        response = POST("/api/v1/yhd/gb/voucher-resend", params);
        errorCount(1, response);
        params.put("voucherCode", coupon.eCouponSn);

        //测试券不存在
        params.put("receiveMobile", "");
        resign(params);
        response = POST("/api/v1/yhd/gb/voucher-resend", params);
        errorCount(1, response);
        params.put("receiveMobile", "13472581853");

        coupon.refresh();
        coupon.downloadTimes = 0;
        coupon.save();
        JPA.em().flush();
        resign(params);
        response = POST("/api/v1/yhd/gb/voucher-resend", params);
        errorCount(1, response);

        coupon.refresh();
        coupon.downloadTimes = 3;
        coupon.save();
        JPA.em().flush();
        resign(params);
        response = POST("/api/v1/yhd/gb/voucher-resend", params);
        errorCount(0, response);
        coupon.refresh();
        assertEquals(new Integer("2"), coupon.downloadTimes);
    }

    private TreeMap<String, String> orderInformParams() {
        Goods goods = FactoryBoy.last(Goods.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        TreeMap<String, String> params = new TreeMap<>();
        params.put("orderCode", "abc");
        params.put("productId", "1");
        params.put("productNum", "2");
        params.put("orderAmount", goods.salePrice.multiply(new BigDecimal("2")).toString());
        params.put("createTime", dateFormat.format(new Date()));
        params.put("paidTime", dateFormat.format(new Date(new Date().getTime() + 300000)));
        params.put("userPhone", "13472581853");
        params.put("productPrice", goods.salePrice.toString());
        params.put("groupId", "1");
        params.put("outerGroupId", String.valueOf(goods.getId()));

        return params;
    }

    private void errorCount(int errCount, Http.Response response){
        try{
            JSONObject jsonObject = new JSONObject(response.out.toString()).getJSONObject("response");
            assertEquals(errCount, jsonObject.getInt("errorCount"));
        }catch (JSONException e){
            fail();
        }
    }

    private void resign(TreeMap<String, String> params){
        params.remove("sign");//必须先删除 不能直接覆盖 因为下面要先算不带sign的签名
        params.put("sign", YHDUtil.md5Signature(params, YHDUtil.SECRET_KEY));
    }
}
