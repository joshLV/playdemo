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
        supplierUser.updatePassword(supplierUser, "654321");
        supplierUser.refresh();
        assertEquals(DigestUtils.md5Hex("654321" + supplierUser.passwordSalt), supplierUser.encryptedPassword);
    }
}
