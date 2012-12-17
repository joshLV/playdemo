package unit.controllers;

import controllers.*;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class SupplierHomeControllerInstanceTest extends UnitTest {
    @Test
    public void controllerInstanceTest(){
        assertTrue(new AccountSequences() instanceof Controller);
        assertTrue(new SupplierHome() instanceof Controller);
        assertTrue(new HomeSuppliers() instanceof Controller);
        assertTrue(new SupplierWithdraw() instanceof Controller);
    }
}
