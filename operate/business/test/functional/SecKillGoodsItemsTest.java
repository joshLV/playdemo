package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.sales.Goods;
import models.sales.SecKillGoods;
import models.sales.SecKillGoodsItem;
import models.sales.SecKillGoodsStatus;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static util.DateHelper.afterDays;


/**
 * User: wangjia
 * Date: 12-8-21
 * Time: 下午6:47
 */
public class SecKillGoodsItemsTest extends FunctionalTest {


    @Before
    public void setUp() {
        FactoryBoy.delete(Goods.class);
        FactoryBoy.delete(SecKillGoods.class);
        FactoryBoy.delete(SecKillGoodsItem.class);

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Http.Response response = GET("/seckill_goods/" + item.secKillGoods.id + "/item");
        assertStatus(200, response);
    }

    @Test
    public void testAdd() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Http.Response response = GET("/seckill_goods/" + item.secKillGoods.id + "/new");
        assertStatus(200, response);
    }


    @Test
    public void testCreateError() {


        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class, "error");


        Map<String, String> itemParams = new HashMap<>();
        itemParams.put("secKillGoodsItem.virtualInventory", item.virtualInventory.toString());
        itemParams.put("secKillGoodsItem.goodsTitle", item.goodsTitle);
        itemParams.put("secKillGoodsItem.saleCount", String.valueOf(item.saleCount));
        itemParams.put("secKillGoodsItem.secKillBeginAt", item.secKillBeginAt.toString());
        itemParams.put("secKillGoodsItem.secKillEndAt", item.secKillEndAt.toString());
        SecKillGoods goods = FactoryBoy.create(SecKillGoods.class);
        Supplier supplier = FactoryBoy.create(Supplier.class);
        itemParams.put("secKillGoodsItem.secKillGoods", item.secKillGoods.toString());
        Http.Response response = POST("/seckill_goods/" + item.secKillGoods.id + "/item", itemParams);
        assertStatus(200, response);
    }


    @Test
    public void testCreate() {
        Date date = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Map<String, String> itemParams = new HashMap<>();
        itemParams.put("secKillGoodsItem.virtualInventory", item.virtualInventory.toString());
        itemParams.put("secKillGoodsItem.goodsTitle", item.goodsTitle);
        itemParams.put("secKillGoodsItem.saleCount", String.valueOf(item.saleCount));
        itemParams.put("secKillGoodsItem.secKillBeginAt", "2012-08-05 21:40:45");
        itemParams.put("secKillGoodsItem.secKillEndAt", dateFormat.format(date));
        itemParams.put("secKillGoodsItem.salePrice", "100");
        itemParams.put("secKillGoodsItem.baseSale", "100");
        SecKillGoods goods = FactoryBoy.create(SecKillGoods.class);
        Supplier supplier = FactoryBoy.create(Supplier.class);
        itemParams.put("secKillGoodsItem.secKillGoods.id", item.secKillGoods.id.toString());
        itemParams.put("secKillGoodsItem.secKillGoods.goods.id", item.secKillGoods.goods.id.toString());

        itemParams.put("secKillGoodsItem.secKillGoods.name", item.secKillGoods.goods.name);
        itemParams.put("secKillGoodsItem. secKillGoods.personLimitNumber", "1");

        itemParams.put("secKillGoodsItem.secKillGoods.supplierId", supplier.id.toString());
        itemParams.put("secKillGoodsItem.secKillGoods.salePrice", BigDecimal.TEN.toString());
        itemParams.put("secKillGoodsItem.secKillGoods.expireAt", afterDays(new Date(), 30).toString());
        Http.Response response = POST("/seckill_goods/" + item.secKillGoods.id + "/item", itemParams);
        assertStatus(302, response);
    }


    @Test
    public void testEdit() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Http.Response response = GET("/seckill_goods/" + item.secKillGoods.id + "/item/" + item.id + "/edit");
        assertStatus(200, response);
    }


    @Test
    public void testUpdateError() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class, "error");
        SecKillGoods goods = FactoryBoy.create(SecKillGoods.class);
        String itemParams = "secKillGoodsItem.virtualInventory=" + item.virtualInventory.toString()
                + "&secKillGoodsItem.goodsTitle=" + item.goodsTitle + "&secKillGoodsItem.saleCount="
                + "" + item.saleCount + "&secKillGoodsItem.secKillBeginAt=" + item.secKillBeginAt.toString()
                + "&secKillGoodsItem.secKillEndAt=" + item.secKillEndAt.toString() + "&secKillGoodsItem.secKillGoods="
                + goods.toString();

        Http.Response response = PUT("/seckill_goods/" + item.secKillGoods.id + "/item/" + item.id, "application/x-www-form-urlencoded", itemParams);
        assertStatus(200, response);
    }


    @Test
    public void testUpdate() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        SecKillGoods goods = FactoryBoy.create(SecKillGoods.class);
        String itemParams = "secKillGoodsItem.virtualInventory=" + item.virtualInventory.toString()
                + "&secKillGoodsItem.goodsTitle=" + item.goodsTitle + "&secKillGoodsItem.saleCount="
                + "" + item.saleCount + "&secKillGoodsItem.secKillBeginAt=" + "2012-08-05 21:40:45"
                + "&secKillGoodsItem.secKillEndAt=" + "2012-08-06 21:40:45" + "&secKillGoodsItem.secKillGoods="
                + goods.toString() + "&secKillGoodsItem.salePrice=100&secKillGoodsItem.baseSale=100";

        Http.Response response = PUT("/seckill_goods/" + item.secKillGoods.id + "/item/" + item.id, "application/x-www-form-urlencoded", itemParams);
        assertStatus(302, response);
    }


    @Test
    public void testDelete() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Http.Response response = DELETE("/seckill_goods/" + item.secKillGoods.id + "/item/" + item.id);
        assertStatus(302, response);
    }


    @Test
    public void testCheckExpireAtError() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        SecKillGoods goods = FactoryBoy.create(SecKillGoods.class);
        String itemParams = "secKillGoodsItem.virtualInventory=" + item.virtualInventory.toString()
                + "&secKillGoodsItem.goodsTitle=" + item.goodsTitle + "&secKillGoodsItem.saleCount="
                + "" + item.saleCount + "&secKillGoodsItem.secKillBeginAt=" + "2012-08-05 21:40:45"
                + "&secKillGoodsItem.secKillEndAt=" + "2012-08-01 21:40:45" + "&secKillGoodsItem.secKillGoods="
                + goods.toString() + "&secKillGoodsItem.salePrice=100&secKillGoodsItem.baseSale=100";

        Http.Response response = PUT("/seckill_goods/" + item.secKillGoods.id + "/item/" + item.id, "application/x-www-form-urlencoded", itemParams);
        assertStatus(200, response);
    }


    /**
     * 修改商品上下架
     */
    @Test

    public void testOnAndOffSale() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Http.Response response = PUT("/seckill_goods/" + item.secKillGoods.id + "/item/" + item.id + "/onSale", "text/html", "");
        assertStatus(302, response);
        item.refresh();
        assertEquals(SecKillGoodsStatus.ONSALE, item.status);
        response = PUT("/seckill_goods/" + item.secKillGoods.id + "/item/" + item.id + "/offSale", "text/html", "");
        item.refresh();
        assertStatus(302, response);
        assertEquals(SecKillGoodsStatus.OFFSALE, item.status);
    }
}