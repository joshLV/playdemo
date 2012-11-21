package unit;

import factory.FactoryBoy;
import models.admin.SupplierUser;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

public class SupplierPasswordUnitTest extends UnitTest {
    SupplierUser supplierUser;

    @Before
    public void setup() {
        FactoryBoy.deleteAll();
        supplierUser = FactoryBoy.create(SupplierUser.class);
    }

    @Test
    public void passwordTest() {
        SupplierUser newSupplierUser = new SupplierUser();
        newSupplierUser.encryptedPassword = "654321";
        String password = supplierUser.encryptedPassword;

        newSupplierUser.updatePassword(supplierUser, newSupplierUser);
        supplierUser.refresh();
        assertNotSame(password, supplierUser.encryptedPassword);
        assertEquals(DigestUtils.md5Hex("654321" + supplierUser.passwordSalt), supplierUser.encryptedPassword);
    }
}
