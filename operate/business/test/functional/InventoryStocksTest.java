package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.sales.InventoryStock;
import models.sales.Sku;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Http;

/**
 * 库存管理的测试用例
 * <p/>
 * User: wangjia
 * Date: 13-3-12
 * Time: 上午10:54
 */
public class InventoryStocksTest {
    InventoryStock stock;

    @Before
    public void setUp() {
        FactoryBoy.delete(InventoryStock.class);
        stock = FactoryBoy.create(InventoryStock.class);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }

    @Test
    public void testIndex() {


    }
}
