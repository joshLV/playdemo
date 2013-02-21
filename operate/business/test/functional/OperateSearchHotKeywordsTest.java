package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.operator.OperateUser;
import models.sales.SearchHotKeywords;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 * 热门关键子查询功能测试
 * .
 * <p/>
 * User: sujie
 * Date: 1/6/13
 * Time: 10:54 AM
 */
public class OperateSearchHotKeywordsTest extends FunctionalTest {
    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        FactoryBoy.batchCreate(20, SearchHotKeywords.class, new SequenceCallback<SearchHotKeywords>() {
            @Override
            public void sequence(SearchHotKeywords target, int seq) {
                target.keywords = "keywords" + seq;
            }
        });

    }

    @Test
    public void testIndex() {
        String searchKeywords = "words";
        Http.Response response = GET("/keywords?searchKeywords=" + searchKeywords);
        assertIsOk(response);

        response = GET("/keywords?searchKeywords=" + searchKeywords + "&page=1");
        assertIsOk(response);

        ModelPaginator<SearchHotKeywords> searchKeywordsPage = (ModelPaginator<SearchHotKeywords>) renderArgs("searchKeywordsPage");
        assertEquals(20, searchKeywordsPage.size());
        String keys = (String) renderArgs("searchKeywords");
        assertEquals(searchKeywords, keys);
    }
}
    