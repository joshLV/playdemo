package functional.finance;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.accounts.Account;
import models.accounts.AccountCreditable;
import models.accounts.AccountSequence;
import models.accounts.AccountStatus;
import models.accounts.AccountType;
import models.accounts.TradeBill;
import models.accounts.util.AccountUtil;
import models.operator.OperateUser;
import models.resale.Resaler;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: tanglq
 * Date: 12-12-11
 * Time: 下午3:24
 */
public class FinancingIncomingNoteTest extends FunctionalTest {

    private Account account;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        Resaler resaler = FactoryBoy.create(Resaler.class);
        account = AccountUtil.getAccount(resaler.id, AccountType.RESALER);
        account.creditable = AccountCreditable.YES;
        account.status = AccountStatus.NORMAL;
        account.save();
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/financing_incoming_add");
        assertIsOk(response);
        List<Account> accounts = (List<Account>) renderArgs("accounts");
        assertEquals(1, accounts.size());
    }

    @Test
    public void testCreate() {
        Map<String, String> params = new HashMap<>();
        params.put("id", account.id.toString());
        params.put("amount", "300");
        Http.Response response = POST("/financing_incoming", params);
        assertEquals(1, TradeBill.count());
        assertEquals(2, AccountSequence.count());
    }

}
