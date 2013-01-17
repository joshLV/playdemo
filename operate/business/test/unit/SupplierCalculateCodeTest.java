package unit;

import factory.FactoryBoy;
import models.supplier.Supplier;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

/**
 * User: wangjia
 * Date: 12-11-30
 * Time: 上午10:45
 */
public class SupplierCalculateCodeTest extends UnitTest {
    String originalCode;

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testCalculateFormattedCode() {
        originalCode = "0001";
        for (int i = 0; i < 30; i++) {
            originalCode = Supplier.calculateFormattedCode(originalCode, "4");
        }
        assertEquals("0031", originalCode);
        originalCode = "01";
        for (int i = 0; i < 30; i++) {
            originalCode = Supplier.calculateFormattedCode(originalCode, "2");
        }
        assertEquals("31", originalCode);
        originalCode = "01";
        for (int i = 0; i < 100; i++) {
            originalCode = Supplier.calculateFormattedCode(originalCode, "2");

        }
        assertEquals("101", originalCode);
    }


}
