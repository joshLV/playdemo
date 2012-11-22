package function;

import com.uhuila.common.util.DateUtil;
import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import models.dangdang.DDAPIInvokeException;
import models.dangdang.DDAPIUtil;
import models.dangdang.ErrorCode;
import models.dangdang.HttpProxy;
import models.dangdang.Response;
import models.order.OuterOrderPartner;
import models.resale.ResalerFav;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.GoodsThirdSupport;
import models.sales.Shop;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.test.FunctionalTest;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-15
 * Time: 下午4:29
 */
public class DDPushGoodsTest extends FunctionalTest {
    Goods goods;
    ResalerFav resalerFav;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        resalerFav = FactoryBoy.create(ResalerFav.class);
        resalerFav.resaler.loginName = "dangdang";
        resalerFav.save();
        goods = resalerFav.goods;
        Security.setLoginUserForTest(resalerFav.resaler.loginName);

    }

    @Test
    public void 测试向当当推送商品_正常情况() {
        Long n = GoodsDeployRelation.count();
        try {
            DDAPIUtil.proxy = new HttpProxy() {
                @Override
                public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
                    Response response = new Response();
                    response.errorCode = ErrorCode.SUCCESS;
                    return response;
                }
            };
            Map<String, Object> goodsArgs = new HashMap<>();
            goodsArgs.put("teamSummary", goods.name);
            goodsArgs.put("teamShortName", goods.shortName);
            goodsArgs.put("teamTitle", goods.title);
            goodsArgs.put("category", "餐饮美食");
            goodsArgs.put("sub_category", "川菜");
            List<Shop> shops = Arrays.asList(goods.getShopList().toArray(new Shop[]{}));
            goodsArgs.put("shops", shops);
            goodsArgs.put("supplierId", goods.supplierId);
            goodsArgs.put("effectiveAt", goods.effectiveAt);
            goodsArgs.put("expireAt", goods.expireAt);
            goodsArgs.put("originalPrice", goods.faceValue);
            goodsArgs.put("salePrice", goods.getResalePrice());
            goodsArgs.put("teamMaxNum", "9999");
            goodsArgs.put("teamMinNum", "1");
            goodsArgs.put("sourceSaleNum", goods.getRealSaleCount());
            goodsArgs.put("limitMaxNum", "99999");
            goodsArgs.put("limitOnceMin", "1");
            goodsArgs.put("limitOnceMax", "999");
            goodsArgs.put("buyTimes", "99999");
            goodsArgs.put("refundType", "1");
            goodsArgs.put("effectStartDate", goods.effectiveAt);
            goodsArgs.put("effectEndDate", goods.expireAt);
            goodsArgs.put("deliveryType", 1);
            goodsArgs.put("srcImage", "http://yibaiquan.com/.jpg");
            goodsArgs.put("teamDetail", "333333333");
            goodsArgs.put("smsHelp", goods.title);

            goodsArgs.put("seoTitle", "啦啦啦啦啦啦啦啦");
            goodsArgs.put("keywords", goods.keywords);
            goodsArgs.put("notice", "");
            GoodsDeployRelation goodsMapping = GoodsDeployRelation.generate(goods, OuterOrderPartner.DD);
            goodsArgs.put("goods", goods);
            goodsArgs.put("goodsMappingId", goods.id);
            Template template = TemplateLoader.load("DDPushGoods/pushGoods1.xml");
            String requestParams = template.render(goodsArgs);
            DDAPIUtil.pushGoods(goodsMapping.linkId, requestParams);

            assertEquals(n + 1, GoodsDeployRelation.count());
        } catch (DDAPIInvokeException e) {
            fail();
        }
    }

    @Test
    public void 测试向当当推送商品_准备页面_从support读取数据() {
        GoodsThirdSupport support = FactoryBoy.create(GoodsThirdSupport.class);
        support.goods = goods;
        support.goodsData = "{\"teamSummary\":\"Product Name1\",\"limitOnceMin\":\"1\",\"limitMaxNum\":\"9999\",\"limitOnceMax\":\"99999\",\"salePrice\":\"5.00\",\"teamMaxNum\":\"10\",\"teamMinNum\":\"1\",\"originalPrice\":\"10.00\",\"teamDetail\":\"teamDetail\",\"teamShortName\":\"Product Name1\",\"teamTitle\":\"Product Title1\",\"beginTime\":\"2012-11-22 00:00:00\",\"buyTimes\":\"9999\",\"effectEndDate\":\"2012-12-22 23:59:59\",\"effectStartDate\":\"2012-11-22 00:00:00\",\"endTime\":\"2012-12-22 23:59:59\",\"goodsId\":\"33\"}";
        support.save();
        Http.Response response = GET("/dangdang-add/" + goods.id);
        assertIsOk(response);

        String name = (String) renderArgs("name");
        assertTrue((name.contains("Product Name")));
        String title = (String) renderArgs("title");
        assertTrue((title.contains("Product Title")));
        String shortName = (String) renderArgs("shortName");
        assertTrue((shortName.contains("Product Name")));

        assertEquals("5.00", renderArgs("salePrice"));
        assertEquals("10.00", renderArgs("faceValue"));
        assertEquals(DateUtil.getBeginOfDay(), renderArgs("effectiveAt"));
        Date afterMonthDate = DateUtil.getEndOfDay(DateHelper.afterDays(30));
        assertEquals(afterMonthDate, renderArgs("expireAt"));
        assertEquals("10", renderArgs("teamMaxNum"));
        assertEquals("1", renderArgs("teamMinNum"));
        assertEquals("9999", renderArgs("limitMaxNum"));
        assertEquals("99999", renderArgs("limitOnceMax"));
        assertEquals("1", renderArgs("limitOnceMin"));
        assertEquals("9999", renderArgs("buyTimes"));
        assertEquals("teamDetail", renderArgs("teamDetail"));
        assertEquals(goods.id, renderArgs("goodsId"));
        assertEquals(1, GoodsThirdSupport.count());
    }

    @Test
    public void 测试向当当推送商品_准备页面() {
        Http.Response response = GET("/dangdang-add/" + goods.id);
        assertIsOk(response);
        String name = (String) renderArgs("name");
        assertTrue((name.contains("Product Name")));
        String title = (String) renderArgs("title");
        assertTrue((title.contains("Product Title")));
        String shortName = (String) renderArgs("shortName");
        assertTrue((shortName.contains("Product Name")));
        assertEquals(new BigDecimal("5.00"), (BigDecimal) renderArgs("salePrice"));
        assertEquals(new BigDecimal("10.00"), (BigDecimal) renderArgs("faceValue"));
        assertEquals(DateUtil.getBeginOfDay(), renderArgs("effectiveAt"));
        Date afterMonthDate = DateUtil.getEndOfDay(DateHelper.afterDays(30));
        assertEquals(afterMonthDate, renderArgs("expireAt"));
        assertEquals(Long.valueOf("10"), (Long) renderArgs("teamMaxNum"));
        assertEquals("1", renderArgs("teamMinNum"));
        assertEquals("9999", renderArgs("limitMaxNum"));
        assertEquals("99999", renderArgs("limitOnceMax"));
        assertEquals("1", renderArgs("limitOnceMin"));
        assertEquals("9999", renderArgs("buyTimes"));
        assertEquals("exhib", renderArgs("exhibition"));
        assertEquals("prompt", renderArgs("prompt"));
        assertEquals("detail", renderArgs("details"));
        assertEquals("des", renderArgs("supplierDes"));
        assertEquals(goods.id, renderArgs("goodsId"));
        assertEquals(0, GoodsThirdSupport.count());
    }
}
