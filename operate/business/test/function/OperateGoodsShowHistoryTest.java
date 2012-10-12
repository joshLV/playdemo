package function;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.sales.Goods;
import models.sales.GoodsHistory;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-10-12
 * Time: 下午3:31
 * To change this template use File | Settings | File Templates.
 */
public class OperateGoodsShowHistoryTest extends FunctionalTest {
    Goods goodsHistory;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        goodsHistory = FactoryBoy.create(Goods.class);
    }

    @Test
    public void testShowHistory() {
        Http.Response response = GET("/goods-history?id=" + goodsHistory.id);

    }
}
