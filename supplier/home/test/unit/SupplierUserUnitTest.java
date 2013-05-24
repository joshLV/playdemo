package unit;

import com.uhuila.common.constants.DeletedStatus;
import factory.FactoryBoy;
import models.admin.SupplierUser;
import models.admin.SupplierUserType;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import play.libs.Images;
import play.modules.paginate.JPAExtPaginator;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

public class SupplierUserUnitTest extends UnitTest {

    SupplierUser supplierUser;

    @org.junit.Before
    public void setup() {
        FactoryBoy.deleteAll();

        supplierUser = FactoryBoy.create(SupplierUser.class);
    }

    //验证是否添加成功
    @Test
    public void testCreate() {
        SupplierUser supplierUser = new SupplierUser();
        supplierUser.loginName = supplierUser.loginName + "1";
        Images.Captcha captcha = Images.captcha();
        String password_salt = captcha.getText(6);
        //密码加密
        supplierUser.encryptedPassword = DigestUtils.md5Hex(supplierUser.encryptedPassword + password_salt);
        //随机吗
        supplierUser.passwordSalt = password_salt;
        supplierUser.lastLoginAt = new Date();
        supplierUser.createdAt = new Date();
        supplierUser.lockVersion = 0;
        supplierUser.deleted = DeletedStatus.UN_DELETED;

        supplierUser.save();
        List<SupplierUser> list = SupplierUser.findAll();
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    //测试是否查询到数据
    @Test
    public void testGetSupplierUserList() {
        int pageNumber = 1;
        int pageSize = 15;
        supplierUser.supplierUserType = SupplierUserType.HUMAN;
        supplierUser.save();

        JPAExtPaginator<SupplierUser> list = SupplierUser.getSupplierUserList(supplierUser.loginName,
                supplierUser.userName, supplierUser.jobNumber, supplierUser.supplier.id,supplierUser.shop.id, pageNumber, pageSize);
        assertEquals(1, list.size());
    }

    //更改用户名和手机
    @Test
    public void testUpdate() {
        SupplierUser newSupplierUser = new SupplierUser();
        newSupplierUser.loginName = "test";
        newSupplierUser.mobile = "13899999999";
        SupplierUser.update(supplierUser.id, newSupplierUser);

        SupplierUser user = SupplierUser.findById(supplierUser.id);
        assertEquals("test", user.loginName);
        assertEquals("13899999999", user.mobile);
    }

    @Test
    public void testUpdatePassword() {
        SupplierUser.updatePassword(supplierUser, "1234567");
        SupplierUser user = SupplierUser.findById(supplierUser.id);

        assertEquals(DigestUtils.md5Hex("1234567" + user.passwordSalt), user.encryptedPassword);
    }

    //测试是否存在用户名和手机
    @Test
    public void testCheckValue() {
        String returnFlag = SupplierUser.checkValue(supplierUser.id, supplierUser.loginName, "", "", supplierUser.supplier.id);
        assertEquals("0", returnFlag);

        returnFlag = SupplierUser.checkValue(supplierUser.id + 1, supplierUser.loginName, "", "", supplierUser.supplier.id);
        assertEquals("1", returnFlag);

        returnFlag = SupplierUser.checkValue(supplierUser.id + 1, supplierUser.loginName + "808", supplierUser.mobile, "", supplierUser.supplier.id);
        assertEquals("2", returnFlag);

        returnFlag = SupplierUser.checkValue(supplierUser.id + 1, supplierUser.loginName + "808", supplierUser.mobile + 1, supplierUser.jobNumber, supplierUser.supplier.id);
        assertEquals("3", returnFlag);

        returnFlag = SupplierUser.checkValue(supplierUser.id + 1, supplierUser.loginName + "808", supplierUser.mobile + 1, supplierUser.jobNumber + 1, supplierUser.supplier.id);
        assertEquals("0", returnFlag);
    }

}
