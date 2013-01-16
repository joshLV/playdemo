package controllers.mock;

import models.order.ECoupon;
import models.order.ECouponPartner;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.ChannelGoodsInfo;
import models.sales.ChannelGoodsInfoStatus;
import models.sales.ImportedCoupon;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.libs.F;
import play.libs.WS;
import play.modules.router.Get;
import play.modules.router.Post;
import play.mvc.Before;
import play.mvc.Controller;
import play.templates.Template;
import play.templates.TemplateLoader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于京东模拟请求测试.
 * User: tanglq
 * Date: 13-1-16
 * Time: 上午11:40
 */
public class JingDongGroupByRequest extends Controller {

    @Before
    public static void checkMockSwitch() {
        String canMock = Play.configuration.getProperty("mock.api.ui", "disable");

        if (!"enabled".equals(canMock)) {
            error(404, "Can't use.");
        }
    }

    /**
     * 订单生成界面.
     */
    @Get("/mock/jingdong/send-order")
    public static void sendOrder() {
        Resaler resaler = Resaler.findOneByLoginName("jingdong");
        List<ChannelGoodsInfo> products = ChannelGoodsInfo.find("resaler=? and status=?", resaler,
                ChannelGoodsInfoStatus.ONSALE).fetch();

        StringBuilder url = new StringBuilder("http://");
        url.append(request.host);
        url.append("/api/v1/jd/gb/send-order?encrypt=false");

        render(products, url);
    }

    /**
     * 生成订单.
     * @param productId
     * @param mobile
     * @param buyNumber
     */
    @Post("/mock/jingdong/send-order")
    public static void doSendOrder(String url, Long productId, String mobile, Integer buyNumber) {
        Map<String, Object> tParams = getSenderOrderParams(productId, mobile, buyNumber);
        String content = renderXmlTemplateContent("mock/JingDongGroupByRequest/sendOrder.xml", tParams);

        String result = doPostRequestBody(url, content);

        render("mock/JingDongGroupByRequest/result.html", content, result);
    }

    @Get("/mock/jingdong/send-sms")
    public static void sendSMS() {
        StringBuilder url = new StringBuilder("http://");
        url.append(request.host);
        url.append("/api/v1/jd/gb/send-sms?encrypt=false");

        // 得到最近的京东券号
        List<ECoupon> ecoupons = ECoupon.find("partner=? order by id desc", ECouponPartner.JD).fetch(20);
        render(ecoupons, url);
    }

    @Post("/mock/jingdong/send-sms")
    public static void doSendSMS(Long ecouponId, String eCouponSn, String mobile, String url) {
        ECoupon ecoupon = null;
        if (StringUtils.isNotEmpty(eCouponSn)) {
            ecoupon = ECoupon.find("eCouponSn=?", eCouponSn).first();
        } else {
            ecoupon = ECoupon.findById(ecouponId);
        }
        if (ecoupon == null) {
            sendSMS(); //找不到券.
        }

        Map<String, Object> tParams = getSendSmsParams(ecoupon, mobile);
        String content = renderXmlTemplateContent("mock/JingDongGroupByRequest/sendSMS.xml", tParams);

        String result = doPostRequestBody(url, content);

        render("mock/JingDongGroupByRequest/result.html", content, result);

    }

    private static String doPostRequestBody(String url, String content) {

        F.Promise<WS.HttpResponse> rsp = WS.url(url).body(content).postAsync();

        F.Promise<List<WS.HttpResponse>> promises = F.Promise.waitAll(rsp);

        // Suspend processing here, until all three remote calls are complete.
        List<WS.HttpResponse> httpResponses = await(promises);
        return httpResponses.get(0).getString();
    }

    private static String renderXmlTemplateContent(String templatePath, Map<String, Object> tParams) {
        Template template = TemplateLoader.load(templatePath);
        return template.render(tParams);
    }


    private static Map<String, Object> getSenderOrderParams(Long productId, String mobile,
                                                            Integer buyNumber) {
        ChannelGoodsInfo product = ChannelGoodsInfo.findById(productId);
        ResalerFav fav = ResalerFav.findByGoodsId(product.resaler, product.goods.id);

        Map<String, Object> tParams = new HashMap<>();
        tParams.put("thirdOrderId", System.currentTimeMillis());
        tParams.put("thirdProductId", fav.thirdGroupbuyId);
        tParams.put("goodsId", fav.lastLinkId);
        tParams.put("mobile", mobile);
        tParams.put("buyNumber", buyNumber);
        tParams.put("price", product.goods.salePrice.multiply(new BigDecimal(100)).setScale(0));
        tParams.put("amount", product.goods.salePrice.multiply(new BigDecimal(buyNumber)).multiply(new BigDecimal(100))
                .setScale(0));

        // 用作生成券信息.
        List<ImportedCoupon> coupons = new ArrayList<>();
        for (int i = 0; i < buyNumber; i++) {
            ImportedCoupon c = new ImportedCoupon();
            c.id = System.currentTimeMillis() + i;
            c.coupon = "8888887" + i;
            coupons.add(c);
        }
        tParams.put("coupons", coupons);
        tParams.put("ts", (new Date()).getTime()/1000);
        return tParams;
    }

    private static Map<String, Object> getSendSmsParams(ECoupon ecoupon, String mobile) {
        Map<String, Object> tParams = new HashMap<>();
        tParams.put("jdCouponId", ecoupon.partnerCouponId);
        tParams.put("eCouponSn", ecoupon.eCouponSn);
        tParams.put("mobile", mobile);
        return tParams;
    }
}
