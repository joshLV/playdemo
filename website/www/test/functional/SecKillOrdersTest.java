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
import models.sales.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.data.validation.Validation;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import factory.callback.BuildCallback;
import util.DateHelper;

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
    Goods goods;
    SecKillGoodsItem item;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        userInfo = FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        address = FactoryBoy.create(Address.class, new BuildCallback<Address>() {
            @Override
            public void build(Address a) {
                a.user = user;
            }
        });

    }

    // ===================== Preview Order  =========================
    @Test
    public void testValidSecKillTimePreviewOrder() {
        item = FactoryBoy.create(SecKillGoodsItem.class, new BuildCallback<SecKillGoodsItem>() {
            @Override
            public void build(SecKillGoodsItem item) {
                item.salePrice = BigDecimal.TEN;
                item.secKillBeginAt = DateHelper.beforeDays(new Date(), 3);
                item.secKillEndAt = DateHelper.afterDays(new Date(), 3);
            }
        });
        item.refresh();
        Http.Response response = GET("/seckill-orders?secKillGoodsItemId=" + item.id);
        assertStatus(200, response);
        assertContentMatch("确认订单信息", response);
        assertEquals(new BigDecimal("10.00"), ((BigDecimal) renderArgs("totalAmount")).setScale(2));
    }

    @Test
    public void testInvalidBeforeSecKillTimePreviewOrder() {
        item = FactoryBoy.create(SecKillGoodsItem.class, new BuildCallback<SecKillGoodsItem>() {
            @Override
            public void build(SecKillGoodsItem item) {
                item.salePrice = BigDecimal.TEN;
                item.secKillBeginAt = DateHelper.beforeDays(new Date(), 10);
                item.secKillEndAt = DateHelper.beforeDays(new Date(), 3);
            }
        });
        item.refresh();
        Http.Response response = GET("/seckill-orders?secKillGoodsItemId=" + item.id);
        assertStatus(500, response);
    }

    @Test
    public void testInvalidAfterSecKillTimePreviewOrder() {
        item = FactoryBoy.create(SecKillGoodsItem.class, new BuildCallback<SecKillGoodsItem>() {
            @Override
            public void build(SecKillGoodsItem item) {
                item.salePrice = BigDecimal.TEN;
                item.secKillBeginAt = DateHelper.afterDays(new Date(), 3);
                item.secKillEndAt = DateHelper.afterDays(new Date(), 5);
            }
        });
        item.refresh();
        Http.Response response = GET("/seckill-orders?secKillGoodsItemId=" + item.id);
        assertStatus(500, response);
    }

    @Test
    public void testPreviewOrderWithoutInventory() {
        SecKillGoodsItem item = FactoryBoy.create(SecKillGoodsItem.class, "noInventory");
        Http.Response response = GET("/seckill-orders?secKillGoodsItemId=" + item.id);
        assertStatus(302, response);
        String location = response.getHeader("Location");
        assertEquals("/seckill-goods", location);
    }

    @Test
    public void testNoSecKillGoodsItemPreviewOrder() {
        Http.Response response = GET("/seckill-orders");
        assertStatus(500, response);
        assertContentMatch("错误", response);
    }

    @Test
    public void testExceedLimitPreviewOrder() {
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.limitNumber = 1;
            }
        });
        item = FactoryBoy.create(SecKillGoodsItem.class);
        final SecKillGoods secKillGoods = FactoryBoy.create(SecKillGoods.class);
        final Order order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.userId = user.id;
                o.userType = AccountType.CONSUMER;
            }
        });
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
        item.refresh();
        Http.Response response = GET("/seckill-orders?secKillGoodsItemId=" + item.id);
        assertStatus(200, response);
        assertEquals(true, (Boolean) renderArgs("exceedLimit"));
    }

    @Test
    public void testOffSaleSecKillGoodsItemPreviewOrder() {
        item = FactoryBoy.create(SecKillGoodsItem.class, new BuildCallback<SecKillGoodsItem>() {
            @Override
            public void build(SecKillGoodsItem item) {
                item.salePrice = BigDecimal.TEN;
                item.secKillBeginAt = DateHelper.beforeDays(new Date(), 3);
                item.secKillEndAt = DateHelper.afterDays(new Date(), 3);
                item.status = SecKillGoodsStatus.OFFSALE;
            }
        });
        item.refresh();
        Http.Response response = GET("/seckill-orders?secKillGoodsItemId=" + item.id);
        assertStatus(500, response);
    }

    // ===================== Create Order  =========================
    @Test
    public void testValidSecKillTimeCreateOrder() {
        item = FactoryBoy.create(SecKillGoodsItem.class, new BuildCallback<SecKillGoodsItem>() {
            @Override
            public void build(SecKillGoodsItem item) {
                item.salePrice = BigDecimal.TEN;
                item.secKillBeginAt = DateHelper.beforeDays(new Date(), 3);
                item.secKillEndAt = DateHelper.afterDays(new Date(), 3);
            }
        });
        Map<String, String> params = new HashMap<>();
        params.put("secKillGoodsItemId", item.id.toString());
        params.put("secKillGoodsId", item.secKillGoods.id.toString());
        params.put("mobile", user.mobile);
        params.put("remark", "good");
        Http.Response response = POST("/seckill-orders/new", params);
        assertStatus(302, response);
        String location = response.getHeader("Location");
        List<Order> createdOrder = Order.findAll();
        assertEquals("/payment_info/" + createdOrder.get(0).orderNumber, location);
    }

    @Test
    public void testInvalidBeforeSecKillTimeCreateOrder() {
        item = FactoryBoy.create(SecKillGoodsItem.class, new BuildCallback<SecKillGoodsItem>() {
            @Override
            public void build(SecKillGoodsItem item) {
                item.salePrice = BigDecimal.TEN;
                item.secKillBeginAt = DateHelper.beforeDays(new Date(), 10);
                item.secKillEndAt = DateHelper.beforeDays(new Date(), 3);
            }
        });
        item.refresh();
        Map<String, String> params = new HashMap<>();
        params.put("secKillGoodsItemId", item.id.toString());
        params.put("secKillGoodsId", item.secKillGoods.id.toString());
        params.put("mobile", user.mobile);
        params.put("remark", "good");
        Http.Response response = POST("/seckill-orders/new", params);
        assertStatus(500, response);
    }

    @Test
    public void testInvalidAfterSecKillTimeCreateOrder() {
        item = FactoryBoy.create(SecKillGoodsItem.class, new BuildCallback<SecKillGoodsItem>() {
            @Override
            public void build(SecKillGoodsItem item) {
                item.salePrice = BigDecimal.TEN;
                item.secKillBeginAt = DateHelper.afterDays(new Date(), 3);
                item.secKillEndAt = DateHelper.afterDays(new Date(), 5);
            }
        });
        Map<String, String> params = new HashMap<>();
        params.put("secKillGoodsItemId", item.id.toString());
        params.put("secKillGoodsId", item.secKillGoods.id.toString());
        params.put("mobile", user.mobile);
        params.put("remark", "good");
        Http.Response response = POST("/seckill-orders/new", params);
        assertStatus(500, response);
    }

    @Test
    public void testNoSecKillGoodsItemCreateOrder() {
        Map<String, String> params = new HashMap<>();
        Http.Response response = POST("/seckill-orders/new", params);
        assertStatus(500, response);
        assertContentMatch("错误", response);
    }

    @Test
    public void testCreateOrderWithoutInventory() {
        item = FactoryBoy.create(SecKillGoodsItem.class, "noInventory");
        Map<String, String> params = new HashMap<>();
        params.put("secKillGoodsItemId", item.id.toString());
        params.put("secKillGoodsId", item.secKillGoods.id.toString());
        params.put("mobile", user.mobile);
        params.put("remark", "good");
        Http.Response response = POST("/seckill-orders/new", params);
        assertStatus(302, response);
        String location = response.getHeader("Location");
        assertEquals("/seckill-goods", location);
    }

    @Test
    public void testExceedLimitCreateOrder() {
        goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods g) {
                g.limitNumber = 1;
            }
        });
        item = FactoryBoy.create(SecKillGoodsItem.class);
        final SecKillGoods secKillGoods = FactoryBoy.create(SecKillGoods.class);
        final Order order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.userId = user.id;
                o.userType = AccountType.CONSUMER;
            }
        });
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
        assertStatus(302, response);
        String location = response.getHeader("Location");
        assertEquals("/seckill-goods?exceedLimit=true", location);
    }

    @Test
    public void testCreateValidReal() {
        goods = FactoryBoy.create(Goods.class, "Real");
        item = FactoryBoy.create(SecKillGoodsItem.class, new BuildCallback<SecKillGoodsItem>() {
            @Override
            public void build(SecKillGoodsItem item) {
                item.salePrice = BigDecimal.TEN;
                item.secKillBeginAt = DateHelper.beforeDays(new Date(), 3);
                item.secKillEndAt = DateHelper.afterDays(new Date(), 3);
                item.secKillGoods.goods.materialType = MaterialType.REAL;
            }
        });
        Map<String, String> params = new HashMap<>();
        params.put("secKillGoodsItemId", item.id.toString());
        params.put("secKillGoodsId", item.secKillGoods.id.toString());
        params.put("mobile", "15026666875");
        params.put("remark", "good");
        Http.Response response = POST("/seckill-orders/new", params);
        assertStatus(302, response);
        String location = response.getHeader("Location");
        List<Order> createdOrder = Order.findAll();
        assertEquals("/payment_info/" + createdOrder.get(0).orderNumber, location);
    }

    public void testNoDefaultAddressCreateRealOrder() {
        goods = FactoryBoy.create(Goods.class, "Real");
        item = FactoryBoy.create(SecKillGoodsItem.class, new BuildCallback<SecKillGoodsItem>() {
            @Override
            public void build(SecKillGoodsItem item) {
                item.salePrice = BigDecimal.TEN;
                item.secKillBeginAt = DateHelper.beforeDays(new Date(), 3);
                item.secKillEndAt = DateHelper.afterDays(new Date(), 3);
            }
        });
        item.refresh();
        address.isDefault = false;
        address.save();
        address.refresh();
        Map<String, String> params = new HashMap<>();
        params.put("secKillGoodsItemId", item.id.toString());
        params.put("secKillGoodsId", item.secKillGoods.id.toString());
        params.put("mobile", user.mobile);
        params.put("remark", "good");
        Http.Response response = POST("/seckill-orders/new", params);
        assertStatus(200, response);
        assertNull(renderArgs("address"));
    }

    @Test
    public void testOffSaleSecKillGoodsItemCreateOrder() {
        item = FactoryBoy.create(SecKillGoodsItem.class, new BuildCallback<SecKillGoodsItem>() {
            @Override
            public void build(SecKillGoodsItem item) {
                item.salePrice = BigDecimal.TEN;
                item.secKillBeginAt = DateHelper.beforeDays(new Date(), 3);
                item.secKillEndAt = DateHelper.afterDays(new Date(), 3);
                item.status = SecKillGoodsStatus.OFFSALE;
            }
        });
        Map<String, String> params = new HashMap<>();
        params.put("secKillGoodsItemId", item.id.toString());
        params.put("secKillGoodsId", item.secKillGoods.id.toString());
        params.put("mobile", user.mobile);
        params.put("remark", "good");
        Http.Response response = POST("/seckill-orders/new", params);
        assertStatus(500, response);
    }
}
