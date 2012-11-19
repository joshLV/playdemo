package function;

import controllers.modules.resale.cas.Security;
import factory.FactoryBoy;
import models.dangdang.DDAPIInvokeException;
import models.dangdang.DDAPIUtil;
import models.dangdang.ErrorCode;
import models.dangdang.HttpProxy;
import models.dangdang.Response;
import models.order.OuterOrderPartner;
import models.resale.ResalerFav;
import models.sales.Category;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Test;
import play.templates.Template;
import play.templates.TemplateLoader;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-15
 * Time: 下午4:29
 */
public class DDPushGoodsTest extends FunctionalTest {
    Goods goods;
    ResalerFav resalerFav;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        resalerFav = FactoryBoy.create(ResalerFav.class);
        resalerFav.resaler.loginName = "dangdang";
        resalerFav.save();
        goods = resalerFav.goods;
        Security.setLoginUserForTest(resalerFav.resaler.loginName);

    }

    @Test
    public void 测试向当当推送商品_正常情况() {
        Long n = GoodsDeployRelation.count();
        try {
            DDAPIUtil.proxy = new HttpProxy() {
                @Override
                public Response accessHttp(PostMethod postMethod) throws DDAPIInvokeException {
                    Response response = new Response();
                    response.errorCode = ErrorCode.SUCCESS;
                    return response;
                }
            };
            Map<String, Object> goodsArgs = new HashMap<>();
            List<Category> categoryList = Category.findByParent(0);//获取顶层分类
            Long categoryId = 0L;
            if (categoryList.size() > 0) {
                if (goods.categories != null && goods.categories.size() > 0 && goods.categories.iterator() != null && goods.categories.iterator().hasNext()) {
                    Category category = goods.categories.iterator().next();
                    categoryId = category.id;

                    if ((goods.topCategoryId == null || goods.topCategoryId == 0) && category.parentCategory != null) {
                        goods.topCategoryId = category.parentCategory.id;
                    }
                }
                if (goods.topCategoryId == null) {
                    goods.topCategoryId = categoryList.get(0).id;
                }
            }
            GoodsDeployRelation goodsMapping = GoodsDeployRelation.generate(goods, OuterOrderPartner.DD);
            goodsArgs.put("categoryId", categoryId);
            goodsArgs.put("goods", goods);
            goodsArgs.put("goodsMappingId", goods.id);
            Template template = TemplateLoader.load("DDPushGoods/pushGoods.xml");
            String requestParams = template.render(goodsArgs);
            DDAPIUtil.pushGoods(goodsMapping.linkId, requestParams);

            assertEquals(n + 1, GoodsDeployRelation.count());
        } catch (DDAPIInvokeException e) {
            fail();
        }
    }

}
