package unit;

import java.util.List;

import models.consumer.User;
import models.consumer.UserInfo;
import models.sales.Category;
import models.sales.Goods;
import models.sales.Shop;
import models.supplier.Supplier;

import org.junit.Before;
import org.junit.Test;

import play.i18n.Messages;
import play.test.UnitTest;
import controllers.modules.website.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;

/**
 * User: wangjia
 * Date: 12-10-8
 * Time: 上午11:33
 */
public class GoodsRelatedToTuanTest extends UnitTest {
    UserInfo userInfo;
    User user;
    Supplier supplier;
    Category category;
    Shop shop;
    Goods goods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        userInfo = FactoryBoy.create(UserInfo.class);
        user = FactoryBoy.create(User.class);
        supplier = FactoryBoy.create(Supplier.class);
        category = FactoryBoy.create(Category.class);
        shop = FactoryBoy.create(Shop.class);
        goods = FactoryBoy.create(Goods.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
    }


    @Test
    public void testTuanSendMail() {
        Messages.formatString("餐饮美食", "tuanCategoryTest." + goods.getCategories().iterator().next().id);
        FactoryBoy.batchCreate(2, Category.class,
                new SequenceCallback<Category>() {
                    @Override
                    public void sequence(Category target, int seq) {
                        target.name = "饮食" + seq;
                    }
                });

        FactoryBoy.batchCreate(2, Goods.class,
                new SequenceCallback<Goods>() {
                    @Override
                    public void sequence(Goods target, int seq) {
                        target.name = "Product" + seq;
                        target.title = "Title" + seq;
                    }
                });
        List<Goods> goodsList = Goods.getTopGoods(category.id, "tuanCategoryTest", "tuanTest", 6);
        assertEquals(0, goodsList.size());
    }
}
