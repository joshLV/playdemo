package unit;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import models.operator.OperateUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import play.libs.Images;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

public class OperateUserUnitTest extends UnitTest {
    OperateUser operateUser;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();
        operateUser = FactoryBoy.create(OperateUser.class);
    }

    //验证是否添加成功
    @Test
    public void testCreate() {
        List<OperateUser> list = OperateUser.findAll();
        int count = list.size();
        OperateUser cuser = new OperateUser();
        Images.Captcha captcha = Images.captcha();
        String password_salt = captcha.getText(6);
        //密码加密
        cuser.encryptedPassword = DigestUtils.md5Hex(cuser.encryptedPassword + password_salt);
        //随机吗
        cuser.passwordSalt = password_salt;
        cuser.lastLoginAt = new Date();
        cuser.createdAt = new Date();
        cuser.lockVersion = 0;
        cuser.deleted = DeletedStatus.UN_DELETED;

        cuser.save();
        list = OperateUser.findAll();
        assertNotNull(list);
        assertTrue(list.size() == count + 1);
    }

    //测试是否查询到数据
    @Test
    public void testGetCuserList() {
        String loginName = "tom";
        int pageNumber = 1;
        int pageSize = 15;
        List<OperateUser> list = OperateUser.getSupplierUserList(loginName, pageNumber, pageSize);
        assertEquals(1, list.size());
    }

    //更改用户名和手机
    @Test
    public void testUpdate() {
        OperateUser cuser = new OperateUser();
        cuser.loginName = "test";
        cuser.mobile = "13899999999";
        OperateUser.update(operateUser.id, cuser);
        OperateUser cusers = OperateUser.findById(operateUser.id);
        assertEquals("test", cusers.loginName);
        assertEquals("13899999999", cusers.mobile);
    }

    //测试是否存在用户名和手机
    @Test
    public void testCheckValue() {
        FactoryBoy.create(OperateUser.class);
        String returnFlag = OperateUser.checkValue(operateUser.id, "tom", "");
        assertEquals("1", returnFlag);

        returnFlag = OperateUser.checkValue(operateUser.id, "808", "13900118888");
        assertEquals("2", returnFlag);

        returnFlag = OperateUser.checkValue(operateUser.id, "808", "1300000003");
        assertEquals("0", returnFlag);
    }

    @Test
    public void testUpdatePassword() {
        OperateUser newUser = OperateUser.findById(operateUser.id);
        OperateUser.updatePassword(newUser, "1234567");

        OperateUser user = OperateUser.findById(operateUser.id);

        assertEquals(DigestUtils.md5Hex("1234567" + user.passwordSalt), user.encryptedPassword);

    }
}
