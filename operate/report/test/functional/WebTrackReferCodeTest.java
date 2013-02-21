package functional;

import java.util.regex.Pattern;

import models.operator.OperateUser;
import models.consumer.UserWebIdentification;
import models.webop.WebTrackRefererReport;
import operate.rbac.RbacLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.ValuePaginator;
import play.mvc.Http.Response;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;
import util.DateHelper;
import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.BuildCallback;

public class WebTrackReferCodeTest extends FunctionalTest {

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);
        
        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        
        FactoryBoy.create(UserWebIdentification.class);
        FactoryBoy.create(UserWebIdentification.class, new BuildCallback<UserWebIdentification>() {
            @Override
            public void build(UserWebIdentification uwi) {
                uwi.createdAt = DateHelper.t("2012-07-17");
                uwi.referCode = "joinme";
            }
        });
    }

    @After
    public void tearDown() {
        // 清除登录Mock
        Security.cleanLoginUserForTest();
    }

    @Test
    public void testDefaultIndex() {
        Response response = GET("/webop/refcodes");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }
 
    @Test
    public void testSearchReferCodeLike() {
        Response response = GET("/webop/refcodes?condition.begin=2012-07-16&condition.end=2012-07-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>)renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
        assertContentMatch("joinme", response);
    }

    @Test
    public void testSearchReferCodeLikeExists() {
        Response response = GET("/webop/refcodes?condition.referCodeLike=join&condition.begin=2012-07-16&condition.end=2012-07-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>)renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
        assertContentMatch("joinme", response);
    }    
       
    @Test
    public void testSearchReferCodeLikeNotExists() {
        Response response = GET("/webop/refcodes?condition.referCodeLike=notexist&condition.begin=2012-07-16&condition.end=2012-07-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>)renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }    
       
    @Test
    public void testSearchReferCodeLikeOutOfRange() {
        Response response = GET("/webop/refcodes?condition.begin=2012-06-16&condition.end=2012-06-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>)renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }

    public static void assertContentNotMatch(String pattern, Response response) {
        Pattern ptn = Pattern.compile(pattern);
        boolean ok = ptn.matcher(getContent(response)).find();
        assertTrue("Response content does match '" + pattern + "'", !ok);
    }
}
