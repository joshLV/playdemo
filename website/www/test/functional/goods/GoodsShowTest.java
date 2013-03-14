package functional.goods;

import models.accounts.AccountType;
import models.consumer.User;
import models.order.ECoupon;
import models.order.Order;
import models.sales.Brand;
import models.sales.Goods;
import models.sales.GoodsStatus;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import util.DateHelper;

import com.uhuila.common.constants.DeletedStatus;

import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

public class GoodsShowTest extends FunctionalTest {
    Goods goods;
    Brand brand;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        goods = FactoryBoy.create(Goods.class);
        brand = FactoryBoy.create(Brand.class);
        goods.brand = brand;
        goods.beginOnSaleAt = com.uhuila.common.util.DateUtil.getEndOfDay(DateHelper.beforeDays(goods.effectiveAt, 5));
        goods.endOnSaleAt = com.uhuila.common.util.DateUtil.getEndOfDay(DateHelper.beforeDays(goods.expireAt, 5));
        goods.save();
    }

    @Ignore
    @Test
    public void testNotExists() throws Exception {
        Response response = GET("/p/" + (goods.id + 100l));
        assertStatus(404, response);
    }

    @Ignore
    @Test
    public void testOffsale() throws Exception {
        goods.status = GoodsStatus.OFFSALE;
        goods.save();
        Response response = GET("/p/" + goods.id);
        assertIsOk(response);
        assertContentMatch("已下架", response);
    }

    @Test
    public void testExpired() throws Exception {
        goods.expireAt = DateHelper.beforeDays(goods.expireAt,10000);
        goods.save();
        goods.refresh();
        Response response = GET("/p/" + goods.id);
        assertIsOk(response);
        System.out.println(">>>>>>>>>>>>>>>>>>>>" + response.out.toString());
        assertContentMatch("已下架", response);
    }

    @Ignore
    @Test
    public void testDeleted() throws Exception {
        goods.deleted = DeletedStatus.DELETED;
        goods.save();
        Response response = GET("/p/" + goods.id);
        assertStatus(404, response);
    }

    @Ignore
    @Test
    public void 限购商品() throws Exception {
        goods.limitNumber = 1L;
        goods.save();

        Response response = GET("/p/" + goods.id);
        assertIsOk(response);
        assertContentMatch("限购1件", response);
    }

    @Ignore
    @Test
    public void 已经购买过限购商品() throws Exception {
        goods.limitNumber = 1L;
        goods.save();
        //设置虚拟登录
        final User user = FactoryBoy.create(User.class);
        Security.setLoginUserForTest(user.loginName);

        FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order o) {
                o.userType = AccountType.CONSUMER;
                o.userId = user.id;
            }
        });
        FactoryBoy.create(ECoupon.class);

        Response response = GET("/p/" + goods.id);
        assertIsOk(response);
        assertContentMatch("您已经购买过此商品", response);

        // 注销登录
        Security.cleanLoginUserForTest();
    }

}
