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

public class WebTrackReferersTest extends FunctionalTest {

    @Before
    public void setup() {
        FactoryBoy.deleteAll();

        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        FactoryBoy.create(UserWebIdentification.class);
        FactoryBoy.create(UserWebIdentification.class,
                        new BuildCallback<UserWebIdentification>() {
                            @Override
                            public void build(UserWebIdentification uwi) {
                                uwi.createdAt = DateHelper.t("2012-07-17");
                                uwi.referCode = "joinme";
                                uwi.referer = "http://www.uhuila.com/payment_info";
                                uwi.refererHost = "www.uhuila.com";
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
        Response response = GET("/webop/referers");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }

    @Test
    public void testSearchRefererLike() {
        Response response = GET("/webop/referers?condition.refererLike=uhuila&condition.begin=2012-07-16&condition.end=2012-07-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
        assertContentMatch("payment_info", response);
    }

    @Test
    public void testSearchRefererLikeNotExists() {
        Response response = GET("/webop/referers?condition.refererLike=yibaiqaa&condition.begin=2012-07-16&condition.end=2012-07-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>) renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }

    @Test
    public void testSearchRefererLikeOutOfRange() {
        Response response = GET("/webop/referers?condition.refererLike=uhuila&condition.begin=2012-06-16&condition.end=2012-06-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>) renderArgs("reportPage");
        assertEquals(0, reportPage.getRowCount());
    }

    @Test
    public void testSearchRefererHost() {
        Response response = GET("/webop/referers?condition.refererLike=uhuila&condition.isHost=true&condition.begin=2012-07-16&condition.end=2012-07-18");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
        ValuePaginator<WebTrackRefererReport> reportPage = (ValuePaginator<WebTrackRefererReport>) renderArgs("reportPage");
        assertEquals(1, reportPage.getRowCount());
        assertContentNotMatch("payment_info", response);
    }

    public static void assertContentNotMatch(String pattern, Response response) {
        Pattern ptn = Pattern.compile(pattern);
        boolean ok = ptn.matcher(getContent(response)).find();
        assertTrue("Response content does match '" + pattern + "'", !ok);
    }
}
