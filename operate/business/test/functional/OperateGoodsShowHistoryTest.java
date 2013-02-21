package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.sales.Goods;
import models.sales.GoodsHistory;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.List;

/**
 * User: wangjia
 * Date: 12-10-12
 * Time: 下午3:31
 */
public class OperateGoodsShowHistoryTest extends FunctionalTest {
    Goods goods;
    GoodsHistory goodsHistory;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        goods = FactoryBoy.create(Goods.class);
        goodsHistory = FactoryBoy.create(GoodsHistory.class);
    }

    @Test
    public void testShowHistory() {
        Http.Response response = GET("/goods/" + goods.id + "/histories");
        List<GoodsHistory> goodsHistoryList = (List<GoodsHistory>) renderArgs("goodsHistoryList");
        assertStatus(200, response);
        assertEquals(1, goodsHistoryList.size());
        assertEquals(goods.id, goodsHistoryList.get(0).goodsId);
    }
}
