package functional;

import controllers.WebsiteInjector;
import controllers.modules.website.cas.Security;
import models.cms.CmsQuestion;
import models.consumer.User;
import models.sales.*;
import org.junit.Before;
import org.junit.Test;
import play.cache.Cache;
import play.mvc.Http;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-7
 * Time: 下午3:29
 * To change this template use File | Settings | File Templates.
 */
public class PGUserQuestionFuncTest extends FunctionalTest {

    @Before
    public void setUp(){
        Fixtures.delete(PointGoods.class);


        Fixtures.delete(Shop.class);
        Fixtures.delete(Goods.class);
        Fixtures.delete(Category.class);
        Fixtures.delete(Brand.class);
        Fixtures.delete(Area.class);
        Fixtures.loadModels("Fixture/pointgoods.yml");
        Fixtures.loadModels("Fixture/areas_unit.yml");
        Fixtures.loadModels("Fixture/categories_unit.yml");
        Fixtures.loadModels("Fixture/supplier_unit.yml");
        Fixtures.loadModels("Fixture/brands_unit.yml");
        Fixtures.loadModels("Fixture/shops_unit.yml");
        Fixtures.loadModels("Fixture/goods_unit.yml");
        Fixtures.loadModels("Fixture/user.yml");
    }

    @Test
    public void testAdd(){
        Long id = (Long) Fixtures.idCache.get("models.sales.PointGoods-pointgoods1");
        assertNotNull(id);
        HashMap<String,String> params = new HashMap<>();

        // user id == null & cookie == null
        Http.Response response = POST("/user-question",params);
        assertIsOk(response);
        assertContentMatch("无法获知提问者身份",response);

        // user id == null & cookie != null & content == null
        Http.Request request= FunctionalTest.newRequest();
        Http.Response cookieRequest = GET("/");
        request.cookies = cookieRequest.cookies;
        response = POST(request, "/user-question",params, new HashMap<String, File>());
        assertIsOk(response);
        assertContentMatch("请输入问题",response);

        // goods id == null
        params.put("content","test question");
        response = POST("/user-question",params);
        assertIsOk(response);
        assertContentMatch("该商品无法评论",response);

        // 参数正确
        params.put("goodsId",id.toString());
        response = POST("/user-question",params);
        assertIsOk(response);
        assertContentMatch("test question",response);

    }

    @Test
    public void testMoreQuestionsUser() {
        Long goodsId = (Long) Fixtures.idCache.get("models.sales.PointGoods-pointgoods1");
        Http.Response response = GET("/more-questions?goodsId=" + goodsId+ "&firstResult=0" + "&size=5");// ?goodsId="+goodsId        +"&size=1"
        assertStatus(200, response);
    }


}
