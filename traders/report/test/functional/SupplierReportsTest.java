package functional;

import controllers.supplier.cas.Security;
import models.admin.SupplierRole;
import models.admin.SupplierUser;
import models.sales.Shop;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import navigation.RbacLoader;
import models.sales.Shop;

import java.math.BigDecimal;
import models.report.ShopDailyReport;
import factory.callback.SequenceCallback;
import factory.callback.BuildCallback;
import factory.FactoryBoy;




/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-8-20
 * Time: 上午11:20
 * To change this template use File | Settings | File Templates.
 */
public class SupplierReportsTest extends FunctionalTest {

    @Before
    public void setUp() {

        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);



        SupplierUser user = FactoryBoy.create(SupplierUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

//        FactoryBoy.batchCreate(10, ShopDailyReport.class,
//                new SequenceCallback<ShopDailyReport>() {
//                    @Override
//                    public void sequence(ShopDailyReport target, int seq) {
//                        target.shop.name = "TEST" + seq;
//                        target.shop.save();
//                        target.buyCount = (long)20.4;
//                        target.orderCount = (long)5;
//                        target.originalAmount = BigDecimal.TEN.add(new BigDecimal(seq));
//                    }
//                }
//        );

    }


    @Test
    public void testShowShopReport() {

        Http.Response response = GET("/reports/shop");

        assertStatus(302, response);
        System.out.println("sdfsdfs<<>>>>"+getContent(response));
        assertContentMatch("门店报表",response);

    }






}