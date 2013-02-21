package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.accounts.AccountSequence;
import models.operator.OperateUser;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.Date;
import java.util.List;

/**
 * User: hejun
 * Date: 12-8-24
 * Time: 下午4:21
 */
public class PaymentReportsFuncTest extends FunctionalTest {

    @Before
    public void setUp() {
        FactoryBoy.lazyDelete();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        // 初始化数据
        FactoryBoy.batchCreate(10, AccountSequence.class, new SequenceCallback<AccountSequence>() {
            @Override
            public void sequence(AccountSequence target, int seq) {
                target.tradeId = new Long(seq);
            }
        });

    }

    @Test
    public void testIndex() {
        Http.Response response = GET("/reports/payment?condition.createdAtBegin=2012-01-01&condition.createdAtEnd=2099-08-24&condition.interval=");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }

    @Test
    public void testIndexWithNull() {
        Http.Response response = GET("/reports/payment");
        assertIsOk(response);
        assertNotNull(renderArgs("reportPage"));
    }

    @Test
    public void testDetail_alipay() {
        AccountSequence sequence = AccountSequence.find("order by id desc").first();
//        sequence.account = PaymentReport.alipayAccount;
        sequence.createdAt = new Date();
        sequence.save();

        Http.Response response = GET("/reports/payment-detail?partners=alipay");
        assertIsOk(response);
        List<AccountSequence> sequences = (List) renderArgs("sequences");
        assertNotNull(sequences);
//        assertEquals(1, sequences.size());
    }

    @Test
    public void testDetail_tenpay() {
//        AccountSequence sequence = AccountSequence.find("order by id desc").first();
//        sequence.account = PaymentReport.tenpayAccount;
//        sequence.save();
        Http.Response response = GET("/reports/payment-detail?partners=tenpay");
        assertIsOk(response);
        assertNotNull(renderArgs("sequences"));
    }

    @Test
    public void testDetail_99bill() {
//        AccountSequence sequence = AccountSequence.find("order by id desc").first();
//        sequence.account = PaymentReport.kuaiqianAccount;
//        sequence.save();
        Http.Response response = GET("/reports/payment-detail?partners=99bill");
        assertIsOk(response);
        assertNotNull(renderArgs("sequences"));
    }

}
