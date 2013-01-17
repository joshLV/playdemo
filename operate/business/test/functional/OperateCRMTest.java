package functional;

import controllers.operate.cas.Security;
import factory.FactoryBoy;
import models.admin.OperateUser;
import models.consumer.User;
import models.order.ECoupon;
import models.sales.ConsultRecord;
import operate.rbac.RbacLoader;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-9-25
 * Time: 下午3:06
 */
public class OperateCRMTest extends FunctionalTest {
    User user1;

    @Before
    public void setUp() {

        FactoryBoy.deleteAll();

        // 重新加载配置文件
        VirtualFile file = VirtualFile.open("conf/rbac.xml");
        RbacLoader.init(file);

        OperateUser user = FactoryBoy.create(OperateUser.class);
        // 设置测试登录的用户名
        Security.setLoginUserForTest(user.loginName);

        user1 = FactoryBoy.lastOrCreate(User.class);
    }

    @Test
    public void getPhoneTest() {
        Http.Response response = GET("/callcenter/phone");
        assertStatus(200, response);
    }


    @Test
    public void indexConditionNullTest() {
        String phone = "15026682165";
        String record = "1028";
        Http.Response response = GET("/callcenter/phone/" + phone + "/record/" + record);
        assertStatus(200, response);
    }

    @Test
    public void indexConditionNotNullTest() {
        String phone = "15026682165";
        String record = "1028";
        Http.Response response = GET("/callcenter/phone/" + phone + "/record/" + record + "?condition.userId=" + user1.id);
        assertStatus(200, response);
    }

    @Test
    public void indexSearchOrderCouponTest() {
        String phone = "15026682165";
        String record = "1028";
        Http.Response response = GET("/callcenter/phone/" + phone + "/record/" + record + "?condition.searchOrderCoupon=" + "456" + "&condition.userId=" + user1.id);
        assertStatus(200, response);

    }

    @Test
    public void indexSearchUserTest() {
        String phone = "15026682165";
        String record = "1028";
        Http.Response response = GET("/callcenter/phone/" + phone + "/record/" + record + "?condition.searchUser=" + "123");
        assertStatus(200, response);

    }


    @Test
    public void tempSaveErrorTest() {
        ConsultRecord consult = FactoryBoy.create(ConsultRecord.class);
        Map<String, String> params = new HashMap<>();
//        params.put("consultId", consult.id.toString());
        params.put("condition.text", "1234");
        Http.Response response = POST("/callcenter/phone/1/record/" + consult.id + "/tempSave?consultId=" + consult.id + "&condition.text=111", params);
        assertStatus(302, response);
    }

    @Test
    public void tempSaveErrorConditionNotNullTest() {
        ConsultRecord consult = FactoryBoy.create(ConsultRecord.class);
        Map<String, String> params = new HashMap<>();
        params.put("consultId", consult.id.toString());

        params.put("user.id", user1.id.toString());
        Http.Response response = POST("/callcenter/phone/1/record/" + consult.id + "/tempSave?condition.searchUser=123", params);
        assertStatus(302, response);
    }

    @Test
    public void tempSaveErrorConditionNotNullUserNullTest() {
        ConsultRecord consult = FactoryBoy.create(ConsultRecord.class);

        Http.Response response = POST("/callcenter/phone/1/record/" + consult.id + "/tempSave?condition.searchUser=123");
        assertStatus(302, response);
    }

    @Test
    public void tempSaveErrorConditionNotNullUserIdTest() {
        ConsultRecord consult = FactoryBoy.create(ConsultRecord.class);
        Http.Response response = POST("/callcenter/phone/1/record/" + consult.id + "/tempSave?condition.searchUser=123");
        assertStatus(302, response);
    }

    @Test
    public void tempSaveTest() {
        ConsultRecord consult = FactoryBoy.create(ConsultRecord.class);
        Http.Response response = POST("/callcenter/phone/1/record/" + consult.id + "/tempSave?consult.text=1234");
        assertStatus(302, response);
    }


    @Test
    public void saveErrorConditionNullTest() {
        ConsultRecord consult = FactoryBoy.create(ConsultRecord.class);
        Map<String, String> params = new HashMap<>();
        params.put("consultId", consult.id.toString());
        Http.Response response = POST("/callcenter/phone/record/save?condition.userId=123", params);
        assertStatus(302, response);
    }

    @Test
    public void saveErrorConditionNotNullTest() {
        ConsultRecord consult = FactoryBoy.create(ConsultRecord.class);
        Http.Response response = POST("/callcenter/phone/record/save?condition.searchUser=123&consultId=" + consult.id);
        assertStatus(302, response);
    }

    @Test
    public void saveTest() {
        ConsultRecord consult = FactoryBoy.create(ConsultRecord.class);
        Http.Response response = POST("/callcenter/phone/record/save?consult.text=1234&consultId=" + consult.id);
        assertStatus(302, response);
    }


    @Test
    public void callCenterTest() {
        Http.Response response = GET("/callcenter/phone/15026682165");
        assertStatus(302, response);
    }


    @Test
    public void bindTest() {
        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        Http.Response response = GET("/crm/bind?couponId=" + coupon.id);
        assertStatus(200, response);
    }


    @Test
    public void saveBindTest() {
        ConsultRecord consult = FactoryBoy.create(ConsultRecord.class);
        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        Http.Response response = GET("/crm/save_bind?couponId=" + coupon.id + "&consultId=" + consult.id);
        assertStatus(200, response);
    }

    @Test
    public void abandonTest() {
        ConsultRecord consult = FactoryBoy.create(ConsultRecord.class);
        Http.Response response = GET("/callcenter/phone/abandon/" + consult.id);
        assertStatus(302, response);
    }

    @Test
    public void bindCouponDetailsTest() {
        ECoupon coupon = FactoryBoy.create(ECoupon.class);
        Http.Response response = GET("/crm/bindCouponDetails?couponId=" + coupon.id);
        assertStatus(200, response);
    }

    @Test
    public void bindSearchUserTest() {
        Http.Response response = GET("/crm/bind_search_user?userId=" + user1.id);
        assertStatus(200, response);
    }

    @Test
    public void jumpIndexTest() {
        Http.Response response = GET("/callcenter/phone/jump_index");
        assertStatus(302, response);
    }

    @Test
    public void jumpPrevIndexTest() {
        Http.Response response = GET("/callcenter/phone/jump_prev_index");
        assertStatus(302, response);
    }


}
