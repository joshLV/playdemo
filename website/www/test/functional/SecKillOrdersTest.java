package functional;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.accounts.AccountType;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;
import models.order.Order;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.SecKillGoods;
import models.sales.SecKillGoodsItem;
import org.junit.Before;
import org.junit.Test;
import play.data.validation.Validation;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-22
 * Time: 下午3:03
 * To change this template use File | Settings | File Templates.
 */
public class SecKillOrdersTest extends FunctionalTest {
    UserInfo userInfo;
    User user;
    Address address;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        userInfo = FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        address = FactoryBoy.create(Address.class);
        address.user = user;
        address.save();
    }


    @Test
    public void testIndex() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Http.Response response = GET("/seckill-orders?secKillGoodsItemId=" + item.id);
        assertStatus(200, response);
        assertContentMatch("确认订单信息", response);

    }

    @Test
    public void testIndexWithoutInventory() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class, "noInventory");
        Http.Response response = GET("/seckill-orders?secKillGoodsItemId=" + item.id);
        assertStatus(302, response);
    }


    @Test
    public void testCreate() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Map<String, String> params = new HashMap<>();
        params.put("secKillGoodsItemId", item.id.toString());
        params.put("secKillGoodsId", item.secKillGoods.id.toString());
        params.put("mobile", "15026666875");
        params.put("remark", "good");
        Http.Response response = POST("/seckill-orders/new", params);
        assertStatus(302, response);
    }

    @Test
    public void testCreateNoSecKillGoodsItem() {
        Http.Response response = POST("/seckill-orders/new");
        assertStatus(500, response);
        assertContentMatch("错误", response);
    }


    @Test
    public void testCreateExceedLimit() {

        final SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        final SecKillGoods secKillGoods = FactoryBoy.create(SecKillGoods.class);
        final Order order = FactoryBoy.create(Order.class);
        order.userId = user.id;
        order.userType = AccountType.CONSUMER;
        order.save();
        FactoryBoy.batchCreate(2, OrderItems.class,
                new SequenceCallback<OrderItems>() {
                    @Override
                    public void sequence(OrderItems target, int seq) {
                        target.buyNumber = 1l;
                        target.order = order;
                        target.secKillGoods = secKillGoods;
                        target.secKillGoodsItemId = item.id;
                        target.goods = secKillGoods.goods;
                        target.status = OrderStatus.PAID;

                    }
                });

        item.secKillGoods = secKillGoods;
        item.save();
        Map<String, String> params = new HashMap<>();
        params.put("secKillGoodsItemId", item.id.toString());
        params.put("secKillGoodsId", item.secKillGoods.id.toString());
        params.put("mobile", "15026666875");
        params.put("remark", "good");
        Http.Response response = POST("/seckill-orders/new", params);

    }

    @Test
    public void testCreateElectronic() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        Goods goods = FactoryBoy.create(Goods.class, "Electronic");
        item.secKillGoods.goods.materialType = MaterialType.ELECTRONIC;
        item.secKillGoods.goods.save();
        item.save();

        Map<String, String> params = new HashMap<>();
        item.refresh();
        params.put("secKillGoodsItemId", item.id.toString());
        params.put("secKillGoodsId", item.secKillGoods.id.toString());
        params.put("mobile", "15026666875");
        params.put("remark", "good");
        Http.Response response = POST("/seckill-orders/new", params);
        assertStatus(302, response);


    }

    @Test
    public void testCreateDefaultAddress() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class);
        address.isDefault = false;
        address.save();
        Map<String, String> params = new HashMap<>();
        params.put("secKillGoodsItemId", item.id.toString());
        params.put("secKillGoodsId", item.secKillGoods.id.toString());
        params.put("mobile", "15026666875");
        params.put("remark", "good");
        Http.Response response = POST("/seckill-orders/new", params);
        assertStatus(302, response);

//        assertTrue(renderArgs(Validation.errors()));
//        Validation v = Validation.current();
//        v.valid(response);
//        assertEquals(false, v.hasErrors());
//        System.out.println("eeeeeeeeeeeeeeeeeeee>>>>>>>>>>>"+Validation.current().hasErrors());
//        System.out.println(Validation.current().equals("请输入收货地址信息!"));
//        System.out.println(Validation.current().toString());


    }


}
