package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.resale.Resaler;
import models.sales.ChannelGoodsInfo;
import models.sales.Goods;
import operate.rbac.RbacLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-5
 * Time: 下午5:42
 */
public class GoodsOnSaleAndOffSaleReportsTest extends FunctionalTest {
    ChannelGoodsInfo channelGoodsInfo;

    @Before
    public void setUp() {

        FactoryBoy.lazyDelete();
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        channelGoodsInfo = FactoryBoy.create(ChannelGoodsInfo.class);
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }
    @Test
    public void testIndex_测试商品上下架_noCondition() {
        Http.Response response = GET("/reports/channel");
        assertIsOk(response);
        List<ChannelGoodsInfo> channelGoodsInfoList = (List) renderArgs("reportPage");
        assertNotNull(channelGoodsInfoList);
        assertEquals(1, channelGoodsInfoList.size());
        List<Resaler> resalerList = (List) renderArgs("resalerList");
        assertNotNull(resalerList);
        assertEquals(1, resalerList.size());
    }

    @Test
    public void testIndex_测试商品上下架_haveCondition() {

        Goods goods = FactoryBoy.create(Goods.class);
        Resaler wubaResaler = FactoryBoy.create(Resaler.class, "wuba");
        ChannelGoodsInfo wuba = FactoryBoy.create(ChannelGoodsInfo.class, "wuba");
        wuba.resaler = wubaResaler;
        wuba.goods = goods;
        wuba.save();

        Resaler jdResaler = FactoryBoy.create(Resaler.class, "jingdong");
        ChannelGoodsInfo jd = FactoryBoy.create(ChannelGoodsInfo.class, "jingdong");
        jd.resaler = jdResaler;
        jd.save();

        Http.Response response = GET("/reports/channel");
        assertIsOk(response);
        List<ChannelGoodsInfo> channelGoodsInfoList = (List) renderArgs("reportPage");
        assertNotNull(channelGoodsInfoList);
        assertEquals(2, channelGoodsInfoList.size());
        List<Resaler> resalerList = (List) renderArgs("resalerList");
        assertNotNull(resalerList);
        assertEquals(3, resalerList.size());
    }

}
