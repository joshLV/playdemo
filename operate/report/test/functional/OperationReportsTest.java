package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import models.*;
import models.accounts.AccountType;
import models.admin.OperateUser;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ValuePaginator;
import play.mvc.Http;
import play.mvc.Router;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 测试运营报表
 * <p/>
 * User: wangjia
 * Date: 13-1-16
 * Time: 下午2:01
 */
public class OperationReportsTest extends FunctionalTest {
    Order order;
    OrderItems orderItems;
    Resaler resaler;
    Supplier supplier;
    ECoupon coupon;
    OperateUser operateUser;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        operateUser = FactoryBoy.create(OperateUser.class);
        Security.setLoginUserForTest(operateUser.loginName);

        resaler = FactoryBoy.create(Resaler.class);
        supplier = FactoryBoy.create(Supplier.class, new BuildCallback<Supplier>() {
            @Override
            public void build(Supplier s) {
                s.salesId = operateUser.id;
            }
        });

        final Goods goods = FactoryBoy.create(Goods.class, new BuildCallback<Goods>() {
            @Override
            public void build(Goods goods) {
                goods.originalPrice = BigDecimal.valueOf(8);
                goods.salePrice = BigDecimal.TEN;
                goods.supplierId = supplier.id;
            }
        });

        final Shop shop = FactoryBoy.create(Shop.class);
        order = FactoryBoy.create(Order.class, new BuildCallback<Order>() {
            @Override
            public void build(Order order) {
                order.status = OrderStatus.PAID;
                order.paidAt = DateHelper.beforeHours(1);
                order.userId = resaler.id;
                order.userType = AccountType.RESALER;
                order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED;
            }
        });
        orderItems = FactoryBoy.create(OrderItems.class);
        coupon = FactoryBoy.create(ECoupon.class);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testIndex() {
        Http.Response response = GET(Router.reverse("OperationReports.index").url);
        assertIsOk(response);
    }

    //    SalesReport
    @Test
    public void testShowSalesReport() {
        Http.Response response = GET(Router.reverse("OperationReports.showSalesReport").url);
        assertIsOk(response);
        assertEquals(1l, ((ValuePaginator<SalesReport>) renderArgs("reportPage")).size());
        assertEquals(BigDecimal.valueOf(20).setScale(2), ((ValuePaginator<SalesReport>) renderArgs("reportPage")).get(0).grossMargin.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((ValuePaginator<SalesReport>) renderArgs("reportPage")).get(0).profit.setScale(2));
    }

    @Test
    public void testSalesReportExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.salesReportExcelOut").url);
        assertIsOk(response);
        assertEquals(1l, ((List<SalesReport>) renderArgs("salesReportList")).size());
        assertEquals(BigDecimal.valueOf(8).setScale(2), ((List<SalesReport>) renderArgs("salesReportList")).get(0).originalPrice.setScale(2));
    }

    @Test
    public void testSalesReportWithPrivilegeExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.salesReportWithPrivilegeExcelOut").url);
        assertIsOk(response);
        assertEquals(1l, ((List<SalesReport>) renderArgs("salesReportList")).size());
        assertEquals(BigDecimal.valueOf(8).setScale(2), ((List<SalesReport>) renderArgs("salesReportList")).get(0).originalPrice.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((List<SalesReport>) renderArgs("salesReportList")).get(0).profit.setScale(2));
    }

    //CategorySalesReport
    @Test
    public void testShowCategorySalesReport() {
        Http.Response response = GET(Router.reverse("OperationReports.showCategorySalesReport").url);
        assertIsOk(response);
        assertEquals(2l, ((ValuePaginator<CategorySalesReport>) renderArgs("reportPage")).size());
        assertEquals(BigDecimal.valueOf(20).setScale(2), ((ValuePaginator<CategorySalesReport>) renderArgs("reportPage")).get(0).grossMargin.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((ValuePaginator<CategorySalesReport>) renderArgs("reportPage")).get(0).profit.setScale(2));
    }

    @Test
    public void testCategorySalesReportExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.categorySalesReportExcelOut").url);
        assertIsOk(response);
        assertEquals(1l, ((List<CategorySalesReport>) renderArgs("resultList")).size());
        assertEquals(BigDecimal.valueOf(8).setScale(2), ((List<CategorySalesReport>) renderArgs("resultList")).get(0).originalPrice.setScale(2));
    }

    @Test
    public void testCategorySalesReportWithPrivilegeExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.categorySalesReportWithPrivilegeExcelOut").url);
        assertIsOk(response);
        assertEquals(1l, ((List<CategorySalesReport>) renderArgs("resultList")).size());
        assertEquals(BigDecimal.valueOf(8).setScale(2), ((List<CategorySalesReport>) renderArgs("resultList")).get(0).originalPrice.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((List<CategorySalesReport>) renderArgs("resultList")).get(0).profit.setScale(2));
    }

    //ChannelReport
    @Test
    public void testShowChannelReport() {
        Http.Response response = GET(Router.reverse("OperationReports.showChannelReport").url);
        assertIsOk(response);
        assertEquals(2l, ((ValuePaginator<ResaleSalesReport>) renderArgs("reportPage")).size());
        assertEquals(BigDecimal.valueOf(10).setScale(2), ((ValuePaginator<ResaleSalesReport>) renderArgs("reportPage")).get(0).salePrice.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((ValuePaginator<ResaleSalesReport>) renderArgs("reportPage")).get(0).profit.setScale(2));
    }

    @Test
    public void testChannelReportExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.channelReportExcelOut").url);
        assertIsOk(response);
        assertEquals(2l, ((List<ResaleSalesReport>) renderArgs("resultList")).size());
        assertEquals(BigDecimal.valueOf(10).setScale(2), ((List<ResaleSalesReport>) renderArgs("resultList")).get(0).salePrice.setScale(2));
    }

    @Test
    public void testChannelReportWithPrivilegeExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.channelReportWithPrivilegeExcelOut").url);
        assertIsOk(response);
        assertEquals(2l, ((List<ResaleSalesReport>) renderArgs("resultList")).size());
        assertEquals(BigDecimal.valueOf(10).setScale(2), ((List<ResaleSalesReport>) renderArgs("resultList")).get(0).salePrice.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((List<ResaleSalesReport>) renderArgs("resultList")).get(0).profit.setScale(2));
    }

    //


    //ConsumerFlowReport
    @Test
    public void testShowConsumerFlowReport() {
        Http.Response response = GET(Router.reverse("OperationReports.showConsumerFlowReport").url);
        assertIsOk(response);
        assertEquals(1l, ((ValuePaginator<ConsumerFlowReport>) renderArgs("reportPage")).size());
        assertEquals(BigDecimal.valueOf(20).setScale(2), ((ValuePaginator<ConsumerFlowReport>) renderArgs("reportPage")).get(0).grossMargin.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((ValuePaginator<ConsumerFlowReport>) renderArgs("reportPage")).get(0).profit.setScale(2));
    }

    @Test
    public void testConsumerFlowReportWithPrivilegeExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.consumerFlowReportWithPrivilegeExcelOut").url);
        assertIsOk(response);
        assertEquals(1l, ((List<ConsumerFlowReport>) renderArgs("resultList")).size());
        assertEquals(BigDecimal.valueOf(10).setScale(2), ((List<ConsumerFlowReport>) renderArgs("resultList")).get(0).salePrice.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((List<ConsumerFlowReport>) renderArgs("resultList")).get(0).profit.setScale(2));
    }

    @Test
    public void testConsumerFlowReportExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.consumerFlowReportExcelOut").url);
        assertIsOk(response);
        assertEquals(1l, ((List<ConsumerFlowReport>) renderArgs("resultList")).size());
        assertEquals(BigDecimal.valueOf(10).setScale(2), ((List<ConsumerFlowReport>) renderArgs("resultList")).get(0).salePrice.setScale(2));
    }

    //PeopleEffectReport
    @Test
    public void testShowPeopleEffectReport() {
        Http.Response response = GET(Router.reverse("OperationReports.showPeopleEffectReport").url);
        assertIsOk(response);
        assertEquals(1l, ((ValuePaginator<SalesReport>) renderArgs("reportPage")).size());
        assertEquals(BigDecimal.valueOf(20).setScale(2), ((ValuePaginator<SalesReport>) renderArgs("reportPage")).get(0).grossMargin.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((ValuePaginator<SalesReport>) renderArgs("reportPage")).get(0).profit.setScale(2));
    }

    @Test
    public void testPeopleEffectReportExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.peopleEffectReportExcelOut").url);
        assertIsOk(response);
        assertEquals(1l, ((List<SalesReport>) renderArgs("peopleEffectReportList")).size());
        assertEquals(BigDecimal.valueOf(10).setScale(2), ((List<SalesReport>) renderArgs("peopleEffectReportList")).get(0).totalAmount.setScale(2));
    }

    //PeopleEffectCategoryReport
    @Test
    public void testShowPeopleEffectCategoryReport() {
        Http.Response response = GET(Router.reverse("OperationReports.showPeopleEffectCategoryReport").url);
        assertIsOk(response);
        assertEquals(2l, ((ValuePaginator<PeopleEffectCategoryReport>) renderArgs("reportPage")).size());
        assertEquals(BigDecimal.valueOf(20).setScale(2), ((ValuePaginator<PeopleEffectCategoryReport>) renderArgs("reportPage")).get(0).grossMargin.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((ValuePaginator<PeopleEffectCategoryReport>) renderArgs("reportPage")).get(0).profit.setScale(2));
    }

    @Test
    public void testPeopleEffectCategoryReportExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.peopleEffectCategoryReportExcelOut").url);
        assertIsOk(response);
        assertEquals(2l, ((List<PeopleEffectCategoryReport>) renderArgs("peopleEffectReportList")).size());
        assertEquals(BigDecimal.valueOf(10).setScale(2), ((List<PeopleEffectCategoryReport>) renderArgs("peopleEffectReportList")).get(0).totalAmount.setScale(2));
    }

    //ChannelCategoryReport
    @Test
    public void testShowChannelCategoryReport() {
        Http.Response response = GET(Router.reverse("OperationReports.showChannelCategoryReport").url);
        assertIsOk(response);
        assertEquals(3l, ((ValuePaginator<ChannelCategoryReport>) renderArgs("reportPage")).size());
        assertEquals(BigDecimal.valueOf(20).setScale(2), ((ValuePaginator<ChannelCategoryReport>) renderArgs("reportPage")).get(0).grossMargin.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((ValuePaginator<ChannelCategoryReport>) renderArgs("reportPage")).get(0).profit.setScale(2));
    }

    @Test
    public void testChannelCategoryReportWithPrivilegeExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.channelCategoryReportWithPrivilegeExcelOut").url);
        assertIsOk(response);
        assertEquals(1l, ((List<ChannelCategoryReport>) renderArgs("resultList")).size());
        assertEquals(BigDecimal.valueOf(10).setScale(2), ((List<ChannelCategoryReport>) renderArgs("resultList")).get(0).salePrice.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((List<ChannelCategoryReport>) renderArgs("resultList")).get(0).profit.setScale(2));
    }

    @Test
    public void testChannelCategoryReportExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.channelCategoryReportExcelOut").url);
        assertIsOk(response);
        assertEquals(1l, ((List<ChannelCategoryReport>) renderArgs("resultList")).size());
        assertEquals(BigDecimal.valueOf(10).setScale(2), ((List<ChannelCategoryReport>) renderArgs("resultList")).get(0).salePrice.setScale(2));
    }

    //ChannelGoodsReport
    @Test
    public void testShowChannelGoodsReport() {
        Http.Response response = GET(Router.reverse("OperationReports.showChannelGoodsReport").url);
        assertIsOk(response);
        assertEquals(2l, ((ValuePaginator<ChannelGoodsReport>) renderArgs("reportPage")).size());
        assertEquals(BigDecimal.valueOf(20).setScale(2), ((ValuePaginator<ChannelGoodsReport>) renderArgs("reportPage")).get(0).grossMargin.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((ValuePaginator<ChannelGoodsReport>) renderArgs("reportPage")).get(0).profit.setScale(2));
    }

    @Test
    public void testChannelGoodsReportWithPrivilegeExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.channelGoodsReportWithPrivilegeExcelOut").url);
        assertIsOk(response);
        assertEquals(2l, ((List<ChannelGoodsReport>) renderArgs("resultList")).size());
        assertEquals(BigDecimal.valueOf(8).setScale(2), ((List<ChannelGoodsReport>) renderArgs("resultList")).get(0).originalPrice.setScale(2));
        assertEquals(BigDecimal.valueOf(2).setScale(2), ((List<ChannelGoodsReport>) renderArgs("resultList")).get(0).profit.setScale(2));
    }

    @Test
    public void testChannelGoodsReportExcelOut() {
        Http.Response response = GET(Router.reverse("OperationReports.channelGoodsReportExcelOut").url);
        assertIsOk(response);
        assertEquals(2l, ((List<ChannelGoodsReport>) renderArgs("resultList")).size());
        assertEquals(BigDecimal.valueOf(8).setScale(2), ((List<ChannelGoodsReport>) renderArgs("resultList")).get(0).originalPrice.setScale(2));
    }

}
