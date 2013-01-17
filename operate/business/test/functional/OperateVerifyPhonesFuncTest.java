package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import models.admin.OperateUser;
import models.admin.SupplierUser;
import models.resale.Resaler;
import models.sales.Shop;
import models.supplier.Supplier;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: hejun
 * Date: 12-8-21
 * Time: 上午11:26
 */
public class OperateVerifyPhonesFuncTest extends FunctionalTest {

    @Before
    public void setUp() {

        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex(){
        final Supplier supplier = FactoryBoy.create(Supplier.class);
        final Shop shop = FactoryBoy.create(Shop.class,"SupplierId", new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.supplierId = supplier.id;
            }
        });

        FactoryBoy.batchCreate(5, SupplierUser.class,
                new SequenceCallback<SupplierUser>() {
                    @Override
                    public void sequence(SupplierUser target, int seq) {
                        target.supplier = supplier;
                        target.shop = shop;
                        target.jobNumber = "000" + seq;
                        target.loginName = "0218888000"+seq;
                        target.mobile = "1351111000"+seq;
                    }
                });
        Http.Response response = GET("/verify-tel?supplierId="+supplier.id);
        assertIsOk(response);
        assertContentType("text/html", response);
        assertNotNull(renderArgs("supplierId"));
        assertNotNull(renderArgs("supplierList"));
    }

    @Test
    public void testAdd(){
        Http.Response response = GET("/verify-tel/new");
        assertIsOk(response);
        assertContentMatch("添加电话验证机",response);
    }

    @Test
    public void testShowSupplierShops(){
        final Supplier supplier = FactoryBoy.create(Supplier.class);
        final Shop shop = FactoryBoy.create(Shop.class,"SupplierId", new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.supplierId = supplier.id;
            }
        });

        Http.Response response = GET("/verify-tel/supplier-shops/"+supplier.id);
        assertIsOk(response);
        assertNotNull(renderArgs("shopList"));
    }

    @Test
    public void testCreate(){
        final Supplier supplier = FactoryBoy.create(Supplier.class);
        final Shop shop = FactoryBoy.create(Shop.class,"SupplierId", new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.supplierId = supplier.id;
            }
        });
        SupplierUser supplierUser = FactoryBoy.create(SupplierUser.class,"Id",new BuildCallback<SupplierUser>() {
            @Override
            public void build(SupplierUser target) {
                target.supplier = supplier;
                target.shop = shop;
            }
        });

        Map<String,String> params = new HashMap<>();
        //params.put("supplierUser.id",supplierUser.id.toString());
        params.put("supplierUser.loginName",supplierUser.loginName);
        params.put("supplierUser.shop",supplierUser.shop.id.toString());

        Http.Response response = POST("/verify-tel",params);
        assertIsOk(response);
        assertContentMatch("商户",response);

    }

    @Test
    public void testDelete(){
        final Supplier supplier = FactoryBoy.create(Supplier.class);
        SupplierUser supplierUser = FactoryBoy.create(SupplierUser.class);

        final Shop shop = FactoryBoy.create(Shop.class,"SupplierId", new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.supplierId = supplier.id;
            }
        });

        FactoryBoy.batchCreate(5, SupplierUser.class,
                new SequenceCallback<SupplierUser>() {
                    @Override
                    public void sequence(SupplierUser target, int seq) {
                        target.supplier = supplier;
                        target.shop = shop;
                        target.jobNumber = "000" + seq;
                        target.loginName = "0218888000"+seq;
                        target.mobile = "1351111000"+seq;
                    }
                });

        Http.Response response = DELETE ("/verify-tel/"+supplierUser.id.toString());
        SupplierUser deleted = SupplierUser.findByUnDeletedId(supplierUser.id);
        assertNull(deleted);
    }

    @Test
    public void testEdit(){
        final Supplier supplier = FactoryBoy.create(Supplier.class);
        final Shop shop = FactoryBoy.create(Shop.class,"SupplierId", new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.supplierId = supplier.id;
            }
        });
        SupplierUser supplierUser = FactoryBoy.create(SupplierUser.class,"Id",new BuildCallback<SupplierUser>() {
            @Override
            public void build(SupplierUser target) {
                target.supplier = supplier;
                target.shop = shop;
            }
        });

        Http.Response response = GET("/verify-tel/"+supplierUser.id.toString()+"/edit");
        assertIsOk(response);
        assertContentMatch("修改电话验证机",response);
    }

    @Test
    public void testUpdate(){
        final Supplier supplier = FactoryBoy.create(Supplier.class);
        final Shop shop = FactoryBoy.create(Shop.class,"SupplierId", new BuildCallback<Shop>() {
            @Override
            public void build(Shop target) {
                target.supplierId = supplier.id;
            }
        });
        SupplierUser supplierUser = FactoryBoy.create(SupplierUser.class,"Id",new BuildCallback<SupplierUser>() {
            @Override
            public void build(SupplierUser target) {
                target.supplier = supplier;
                target.shop = shop;
            }
        });

        Map<String,String> params = new HashMap<>();
        params.put("id",supplierUser.id.toString());
        params.put("loginName","02100000000");
        params.put("shopId",shop.id.toString());

        Http.Response response = POST("/verify-tel/"+supplierUser.id.toString(),params);

        assertStatus(302, response);
        SupplierUser newSupplierUser = SupplierUser.findById(supplierUser.id);
        newSupplierUser.refresh();
        assertEquals("02100000000",newSupplierUser.loginName);

    }


}
