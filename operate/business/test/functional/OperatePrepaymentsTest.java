package functional;

import com.uhuila.common.constants.DeletedStatus;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.operator.OperateUser;
import models.order.Prepayment;
import models.supplier.Supplier;
import org.junit.Test;
import play.data.validation.Error;
import play.modules.paginate.ModelPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预付款管理功能测试.
 * <p/>
 * User: sujie
 * Date: 1/5/13
 * Time: 9:23 AM
 */
public class OperatePrepaymentsTest extends FunctionalTest {
    Prepayment prepayment;
    Supplier supplier;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        FactoryBoy.batchCreate(20, Supplier.class, new SequenceCallback<Supplier>() {
            @Override
            public void sequence(Supplier target, int seq) {
                target.otherName = "otherName" + seq;
            }
        });

        FactoryBoy.batchCreate(20, Prepayment.class, new SequenceCallback<Prepayment>() {
            @Override
            public void sequence(Prepayment target, int seq) {
                target.amount = new BigDecimal(seq);
            }
        });

        supplier = FactoryBoy.create(Supplier.class);
        prepayment = FactoryBoy.create(Prepayment.class);
    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/prepayments?page=1");
        assertIsOk(response);

        List<Supplier> supplierList = (List<Supplier>) renderArgs("supplierList");
        assertEquals(21, supplierList.size());
        ModelPaginator<Prepayment> prepaymentPage = (ModelPaginator<Prepayment>) renderArgs("prepaymentPage");
        assertEquals(21, prepaymentPage.size());
        assertEquals(2, prepaymentPage.getPageCount());

        Prepayment firstPrepayment = prepaymentPage.get(0);
        assertEquals(supplier.otherName, firstPrepayment.supplier.otherName);
        Long supplierId = (Long) renderArgs("supplierId");
        assertNull(supplierId);
    }

    @Test
    public void testIndexSupplierId() {
        Http.Response response = GET("/prepayments?page=1&supplierId=" + supplier.id);
        assertIsOk(response);

        List<Supplier> supplierList = (List<Supplier>) renderArgs("supplierList");
        assertEquals(21, supplierList.size());
        ModelPaginator<Prepayment> prepaymentPage = (ModelPaginator<Prepayment>) renderArgs("prepaymentPage");
        assertEquals(1, prepaymentPage.getRowCount());
        Prepayment firstPrepayment = prepaymentPage.get(0);
        assertEquals(supplier.otherName, firstPrepayment.supplier.otherName);
        Long supplierId = (Long) renderArgs("supplierId");
        assertEquals(supplier.id, supplierId);
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/prepayments/new");
        assertIsOk(response);

        List<Supplier> supplierList = (List<Supplier>) renderArgs("supplierList");
        assertEquals(21, supplierList.size());
    }

    @Test
    public void testCreate() {
        long count = Prepayment.count();

        Map<String, String> params = new HashMap<>();
        params.put("prepayment.supplier.id", String.valueOf(supplier.id));
        params.put("prepayment.amount", "123.12");
        params.put("prepayment.effectiveAt", "2012-12-12");
        params.put("prepayment.expireAt", "2013-12-12");
        params.put("prepayment.remark", "prepayment remark");
        Http.Response response = POST("/prepayments", params);
        assertStatus(302, response);

        assertEquals(count + 1, Prepayment.count());
    }

    @Test
    public void testCreateInvalid() {
        long count = Prepayment.count();

        Map<String, String> params = new HashMap<>();
        params.put("prepayment.amount", "123.12");
        params.put("prepayment.effectiveAt", "2012-12-12");
        params.put("prepayment.expireAt", "2013-12-12");
        params.put("prepayment.remark", "prepayment remark");
        Http.Response response = POST("/prepayments", params);

        List<Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("prepayment.supplier", errors.get(0).getKey());
        assertStatus(200, response);

        assertEquals(count, Prepayment.count());
    }


    @Test
    public void testEdit() {
        Http.Response response = GET("/prepayments/" + prepayment.id + "/edit");
        assertIsOk(response);

        Prepayment editPrepayment = (Prepayment) renderArgs("prepayment");
        assertNotNull(editPrepayment);
        assertEquals(prepayment.id, editPrepayment.id);
        assertEquals(prepayment.remark, editPrepayment.remark);
        List<Supplier> supplierList = (List<Supplier>) renderArgs("supplierList");
        assertEquals(21, supplierList.size());
        Long prepaymentId = (Long) renderArgs("id");
        assertEquals(prepayment.id, prepaymentId);
    }

    @Test
    public void testUpdate() {
        String params = "prepayment.amount=" + 10001 + "&prepayment.supplier.id=" + supplier.id;
        Http.Response response = PUT("/prepayments/" + prepayment.id, "application/x-www-form-urlencoded", params);
        assertStatus(302, response);
        prepayment.refresh();
        assertEquals(0, new BigDecimal(10001).compareTo(prepayment.amount));
    }

    @Test
    public void testUpdateInvalid() {
        String params = "prepayment.amount=" + 10001;
        Http.Response response = PUT("/prepayments/" + prepayment.id, "application/x-www-form-urlencoded", params);
        assertStatus(200, response);
        List<Error> errors = (List<Error>) renderArgs("errors");
        assertEquals("prepayment.supplier", errors.get(0).getKey());
        assertStatus(200, response);
    }


    @Test
    public void testShow() {
        Http.Response response = GET("/prepayments/" + prepayment.id);
        assertIsOk(response);

        Prepayment firstPrepayment = (Prepayment) renderArgs("prepayment");
        assertEquals(firstPrepayment.id, prepayment.id);
    }

    @Test
    public void testDelete() {
        assertEquals(DeletedStatus.UN_DELETED, prepayment.deleted);

        Http.Response response = DELETE("/prepayments/" + prepayment.id);
        assertStatus(302, response);

        prepayment.refresh();
        assertEquals(DeletedStatus.DELETED, prepayment.deleted);
    }


    @Test
    public void testDeleteNull() {
        Http.Response response = DELETE("/prepayments/" + 0);
        assertStatus(302, response);
    }

}
    