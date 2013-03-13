package unit;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import models.accounts.AccountType;
import models.consumer.User;
import models.operator.OperateUser;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import models.order.OrderECouponMessage;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.Shop;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * TODO.
 * <p/>
 * User: wangjia
 * Date: 13-3-5
 * Time: 下午3:51
 */
public class OrderECouponMessageTest extends UnitTest {

    OperateUser user;
    Order order;
    List<ECoupon> couponList;
    OrderItems orderItems;
    List<ECoupon> orderItemsCouponsList;

    Resaler wuba;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();


        // 初始化 电子券数据
        final Goods goods = FactoryBoy.create(Goods.class);
        final Shop shop = FactoryBoy.create(Shop.class);
        order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order order) {
                order.paidAt = DateHelper.beforeHours(1);
            }
        });
        couponList = FactoryBoy.batchCreate(10, ECoupon.class, "Id",
                new SequenceCallback<ECoupon>() {
                    @Override
                    public void sequence(ECoupon target, int seq) {
                        target.shop = shop;
                        target.goods = goods;
                        target.eCouponSn = "8888000" + seq;
                        target.status = ECouponStatus.UNCONSUMED;
                        target.isFreeze = 0;
                        target.createdAt = new Date();
                    }
                });
        wuba = FactoryBoy.create(Resaler.class, new BuildCallback<Resaler>() {
            @Override
            public void build(Resaler target) {
                target.loginName = Resaler.WUBA_LOGIN_NAME;
            }
        });
        User user = FactoryBoy.last(User.class);
        Goods goodsA = FactoryBoy.last(Goods.class);
        Goods goodsB = FactoryBoy.create(Goods.class);
        Order order = FactoryBoy.create(Order.class);
        orderItems = FactoryBoy.create(OrderItems.class);
        order.userId = user.id;
        order.status = OrderStatus.PAID;
        order.save();
        orderItems.phone = user.mobile;
        orderItems.status = OrderStatus.PAID;
        orderItems.save();
        orderItemsCouponsList = FactoryBoy.batchCreate(2, ECoupon.class, "password",
                new SequenceCallback<ECoupon>() {
                    @Override
                    public void sequence(ECoupon target, int seq) {
                        target.status = ECouponStatus.UNCONSUMED;
                        target.isFreeze = 0;
                        target.createdAt = new Date();
                    }
                });


    }

    SimpleDateFormat dateFormat = new SimpleDateFormat(Order.COUPON_EXPIRE_FORMAT);

    @Test
    public void testEcouponGetOrderSMSMessage() {
        assertEquals(couponList.get(0).goods.title + "券号" + couponList.get(0).eCouponSn + ",截止" + dateFormat.format(couponList.get(0).expireAt) + "一百券客服4006262166", OrderECouponMessage.getOrderSMSMessage(couponList.get(0)));
    }

    @Test
    public void testOrderItemsGetOrderSMSMessage() {
        System.out.println(OrderECouponMessage.getOrderSMSMessage(orderItems));
        assertEquals(orderItemsCouponsList.get(0).goods.title + "券号" + orderItemsCouponsList.get(0).eCouponSn + "密码"
                + orderItemsCouponsList.get(0).eCouponPassword + "，券号" + orderItemsCouponsList.get(1).eCouponSn + "密码" +
                orderItemsCouponsList.get(1).eCouponPassword + "[共2张],截止" +
                dateFormat.format(orderItemsCouponsList.get(1).expireAt) + "一百券客服4006262166",
                OrderECouponMessage.getOrderSMSMessage(orderItems));
    }

    @Test
    public void testEcouponGetOrderSMSMessageFor58() {
        order.userType = AccountType.RESALER;
        order.userId = wuba.id;
        order.save();
        StringBuilder sb = new StringBuilder();
        sb.append("【58团】")
                .append(couponList.get(0).goods.title)
                .append("由58合作商家【一百券】提供,一百券号").append(couponList.get(0).eCouponSn)
                .append(",有效期至").append(dateFormat.format(couponList.get(0).expireAt))
                .append("58客服4007895858");

        assertEquals(sb.toString(), OrderECouponMessage.getOrderSMSMessage(couponList.get(0)));
    }

    @Test
    public void testOrderItemsGetOrderSMSMessageFor58() {
        orderItems.order.userType = AccountType.RESALER;
        orderItems.order.userId = wuba.id;
        orderItems.order.save();
        System.out.println(OrderECouponMessage.getOrderSMSMessage(orderItems));

        StringBuilder sb = new StringBuilder();
        sb.append("【58团】")
                .append(orderItemsCouponsList.get(0).goods.title)
                .append("由58合作商家【一百券】提供,一百")
                .append("券号").append(orderItemsCouponsList.get(0).eCouponSn)
                .append("密码").append(orderItemsCouponsList.get(0).eCouponPassword)
                .append("，券号").append(orderItemsCouponsList.get(1).eCouponSn)
                .append("密码").append(orderItemsCouponsList.get(1).eCouponPassword)
                .append("[共2张]")
                .append(",有效期至").append(dateFormat.format(orderItemsCouponsList.get(1).expireAt))
                .append("58客服4007895858");
        assertEquals(sb.toString(), OrderECouponMessage.getOrderSMSMessage(orderItems));
    }

}
