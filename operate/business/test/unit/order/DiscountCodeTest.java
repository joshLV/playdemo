package unit.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import models.order.DiscountCode;
import models.order.Order;
import models.order.OrderDiscount;
import models.order.OrderStatus;

import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.ModelPaginator;
import play.test.UnitTest;
import util.DateHelper;

import com.uhuila.common.constants.DeletedStatus;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;

public class DiscountCodeTest extends UnitTest {

    @Before
    public void setUp() {
        FactoryBoy.lazyDelete();
    }

    @Test
    public void 通过SN找到可用的折扣券() {
        DiscountCode dc = FactoryBoy.create(DiscountCode.class,
                        new BuildCallback<DiscountCode>() {
                            @Override
                            public void build(DiscountCode target) {
                                target.discountSn = "AVAIL";
                                target.discountAmount = BigDecimal.TEN;
                            }
                        });
        System.out.println("dc.begin=" + dc.beginAt + ", end=" + dc.endAt);
        System.out.println("dc.sn=" + dc.discountSn + ", delete=" + dc.deleted);
        System.out.println(DiscountCode.findAvailableSN("AVAIL"));
        assertNotNull(DiscountCode.findAvailableSN("AVAIL"));
        assertNotNull(DiscountCode.findAvailableSN("Avail"));
    }

    @Test
    public void 通过已过期的SN找不到可用的折扣券() {
        FactoryBoy.create(DiscountCode.class, "Unavailable",
                        new BuildCallback<DiscountCode>() {
                            @Override
                            public void build(DiscountCode target) {
                                target.discountSn = "UNAVI";
                                target.discountAmount = BigDecimal.TEN;
                            }
                        });
        assertNull(DiscountCode.findAvailableSN("UNAVI"));
    }

    @Test
    public void 无效的SN会找不到可用的折扣券() throws Exception {
        assertNull(DiscountCode.findAvailableSN("xxxxx"));
        assertNull(DiscountCode.findAvailableSN(null));
    }

    @Test
    public void 列出所有可用折扣券() {
        DiscountCode discountCode = FactoryBoy.create(DiscountCode.class);
        FactoryBoy.batchCreate(5, DiscountCode.class,
                        new SequenceCallback<DiscountCode>() {
                            @Override
                            public void sequence(DiscountCode target, int seq) {
                                target.discountSn = "TEST" + seq;
                            }
                        });
        System.out.println("discountCode=" + discountCode + ", sn="
                        + discountCode.discountSn);
        List<DiscountCode> list = DiscountCode.findAll();
        System.out.println("list.size=" + list.size());
        for (DiscountCode discountCode2 : list) {
            System.out.println(discountCode2.id + ". name="
                            + discountCode2.discountSn);
        }
        assertEquals(6l, DiscountCode.count());

        ModelPaginator discountCodePage = DiscountCode.getDiscountCodePage(0,
                        10, null);
        assertEquals(1, discountCodePage.getPageCount());
        assertEquals(6, discountCodePage.getRowCount());

        ModelPaginator discountCodePage1 = DiscountCode.getDiscountCodePage(0,
                        5, null);
        assertEquals(2, discountCodePage1.getPageCount());
        assertEquals(6, discountCodePage1.getRowCount());
        discountCode.deleted = DeletedStatus.DELETED;
        discountCode.save();
        ModelPaginator discountCodePage2 = DiscountCode.getDiscountCodePage(0,
                        5, null);
        assertEquals(1, discountCodePage2.getPageCount());
        assertEquals(5, discountCodePage2.getRowCount());
    }

    @Test
    public void 更新折扣券信息() {
        DiscountCode discountCode = FactoryBoy.create(DiscountCode.class);
        DiscountCode form = FactoryBoy.build(DiscountCode.class,
                        new BuildCallback<DiscountCode>() {
                            @Override
                            public void build(DiscountCode target) {
                                target.discountSn = "NewSN";
                                target.endAt = DateHelper.afterDays(new Date(),
                                                15);
                                target.deleted = DeletedStatus.DELETED;
                            }
                        });
        DiscountCode.update(discountCode.id, form);
        DiscountCode dc2 = DiscountCode.findById(discountCode.id);
        assertEquals("NEWSN", dc2.discountSn);
        assertEquals(form.endAt, dc2.endAt);
        // update方法不更新删除标志
        assertEquals(DeletedStatus.UN_DELETED, dc2.deleted);
    }

    @Test
    public void 刚发布时此折扣码的已使用数量为0() {
        DiscountCode discountCode = FactoryBoy.create(DiscountCode.class);
        assertEquals(new Long(0), discountCode.getUsedDiscountCodeCount());
    }

    @Test
    public void 测试已使用折扣码的订单数量() {
        final DiscountCode discountCode = FactoryBoy.create(DiscountCode.class);
        FactoryBoy.create(OrderDiscount.class,
                        new BuildCallback<OrderDiscount>() {
                            @Override
                            public void build(OrderDiscount target) {
                                target.order = FactoryBoy.create(Order.class,
                                                new BuildCallback<Order>() {
                                                    @Override
                                                    public void build(
                                                                    Order order) {
                                                        order.status = OrderStatus.PAID;
                                                    }
                                                });
                                target.discountCode = discountCode;
                            }
                        });

        assertEquals(new Long(1), discountCode.getUsedDiscountCodeCount());
    }
}
