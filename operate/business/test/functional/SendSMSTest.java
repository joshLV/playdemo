package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.operator.OperateUser;
import models.sales.SendSMSInfo;
import models.sales.SendSMSTask;
import operate.rbac.RbacLoader;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

/**
 * User: wangjia
 * Date: 12-9-26
 * Time: 下午5:15
 */
public class SendSMSTest extends FunctionalTest {
    SendSMSInfo smsInfo;
    SendSMSTask smsTask;

    @org.junit.Before
    public void setUp() {
        FactoryBoy.delete(OperateUser.class);
        FactoryBoy.delete(SendSMSInfo.class);

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);

        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);
        smsInfo= FactoryBoy.create(SendSMSInfo.class);
        smsTask= FactoryBoy.create(SendSMSTask.class);

    }

    @Test
    public void testIndexConditionNull() {
        Http.Response response = GET("/send_sms");
        assertStatus(200, response);
        assertContentMatch("发送优惠券", response);
    }

    @Test
    public void testIndexConditionNotNull() {
        Http.Response response = GET("/send_sms?condition.taskNo=123");
        assertStatus(200, response);
        assertContentMatch("发送优惠券", response);
    }

    @Test
    public void testDetails() {
        Http.Response response = GET("/send_sms/" + smsInfo.taskNo);
        assertStatus(200, response);
    }

    @Test
    public void testAdd() {
        Http.Response response = GET("/send_sms/new");
        assertStatus(200, response);
    }

    @Test
    public void testCreate() {
        Http.Response response = GET("/send_sms/create?taskTempNo=147&tempMobile=13905689452&tempECouponSn=568923&tempText=123568");
        assertStatus(302, response);
    }

    @Test
    public void testCreateError() {
        Http.Response response = GET("/send_sms/create");
        assertStatus(200, response);
    }

    @Test
    public void testCreateECouponSnLengthWrong() {
        Http.Response response = GET("/send_sms/create?taskTempNo=147&tempMobile=1356&tempECouponSn=123&tempText=123568");
        assertStatus(302, response);
    }

    @Test
    public void testSend() {
        Http.Response response = GET("/send_sms/send?taskTempNo="+smsInfo.taskNo);
        assertStatus(200, response);
    }

    @Test
    public void testSucSendInstantly() {
        Http.Response response = GET("/send_sms/suc_send?taskTempNo="+smsInfo.taskNo+"&timer=0");
        assertStatus(302, response);
    }

    @Test
    public void testSucSendScheduledTime() {
        Http.Response response = GET("/send_sms/suc_send?taskTempNo="+smsInfo.taskNo+"&scheduledTime=2012-09-17 17:27:33&timer=1");
        assertStatus(302, response);
    }




}
