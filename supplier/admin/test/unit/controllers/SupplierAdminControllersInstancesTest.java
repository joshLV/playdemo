package unit.controllers;

import controllers.*;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class SupplierAdminControllersInstancesTest extends UnitTest{
    @Test
    public void controllerInstanceTest(){
        assertTrue(new FoundationJobsApplication() instanceof Controller);
        assertTrue(new SupplierProfiles() instanceof Controller);
        assertTrue(new SuppliersFindPassword() instanceof Controller);
        assertTrue(new SuppliersPassword() instanceof Controller);
        assertTrue(new SupplierAdminUsers() instanceof Controller);
    }
}
