package functional;

import factory.FactoryBoy;
import models.sales.PointGoods;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

import java.io.File;
import java.util.HashMap;

/**
 * User: hejun
 * Date: 12-8-7
 * Time: 下午3:29
 */
public class PGUserQuestionFuncTest extends FunctionalTest {
    PointGoods pointGoods;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        pointGoods = FactoryBoy.create(PointGoods.class);
    }

    @Test
    public void testAdd() {
        HashMap<String, String> params = new HashMap<>();

        // user id == null & cookie == null
        Http.Response response = POST("/user-question", params);
        assertIsOk(response);
        assertContentMatch("无法获知提问者身份", response);

        // user id == null & cookie != null & content == null
        Http.Request request = FunctionalTest.newRequest();
        Http.Response cookieRequest = GET("/");
        request.cookies = cookieRequest.cookies;
        response = POST(request, "/user-question", params, new HashMap<String, File>());
        assertIsOk(response);
        assertContentMatch("请输入问题", response);

        // goods id == null
        params.put("content", "test question");
        response = POST("/user-question", params);
        assertIsOk(response);
        assertContentMatch("该商品无法评论", response);

        // 参数正确
        params.put("goodsId", pointGoods.id.toString());
        response = POST("/user-question", params);
        assertIsOk(response);
        assertContentMatch("test question", response);
    }

    @Test
    public void testMoreQuestionsUser() {
        Http.Response response = GET("/more-questions?goodsId=" + pointGoods.id + "&firstResult=0" + "&size=5");// ?goodsId="+goodsId        +"&size=1"
        assertStatus(200, response);
    }


}
