package unit;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.accounts.AccountSequence;
import models.accounts.AccountSequenceCondition;
import models.accounts.AccountType;
import models.accounts.TradeType;
import models.operator.OperateUser;
import models.webop.BalanceReport;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import play.vfs.VirtualFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: hejun
 * Date: 12-8-24
 * Time: 上午10:16
 */
public class BalanceReportUnitTest extends UnitTest {

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
        BalanceReport balanceReport = new BalanceReport(new Date(), TradeType.PAY,new BigDecimal(10),new BigDecimal(5));
        assertNotNull(balanceReport);
        assertEquals(TradeType.PAY,balanceReport.tradeType);
    }

    @Test
    public void testQueryWithdrawReport(){
        AccountSequenceCondition condition = new AccountSequenceCondition();
        condition.accountTypes = new ArrayList<>();
        condition.accountTypes.add(AccountType.CONSUMER);
        condition.createdAtBegin =  new Date();
        condition.createdAtEnd =  new Date();

        List<BalanceReport> list = BalanceReport.queryWithdrawReport(condition);
        assertNotNull(list);
        assertEquals(1,list.size());

    }
}

