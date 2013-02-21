package unit;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceCondition;
import models.operator.OperateUser;
import models.webop.PaymentReport;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * User: hejun
 * Date: 12-8-24
 * Time: 下午3:29
 */
public class PaymentReportUnitTest extends UnitTest {

    @Before
    public void setUp(){
        FactoryBoy.lazyDelete();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        // 初始化数据
        FactoryBoy.batchCreate(10,AccountSequence.class,new SequenceCallback<AccountSequence>() {
            @Override
            public void sequence(AccountSequence target, int seq) {
                target.tradeId = new Long(seq);
            }
        });

    }

    @Test
    public void testInit(){
        Account account = FactoryBoy.create(Account.class);
        PaymentReport paymentReport = new PaymentReport(new Date(),account,new BigDecimal(10));
        assertNotNull(paymentReport);
    }

    @Test
    public void testQuery(){
        AccountSequenceCondition condition = new AccountSequenceCondition();
        condition.createdAtBegin =  new Date(System.currentTimeMillis() - 6000000);
        condition.createdAtEnd =  new Date(System.currentTimeMillis() + 6000000);

        //System.out.println(AccountSequence.findAll().size());
        List<PaymentReport> list = PaymentReport.queryPaymentReport(condition);
        assertNotNull(list);
//        assertEquals(10,list.size());
        assertEquals(1,list.size());
    }
}
