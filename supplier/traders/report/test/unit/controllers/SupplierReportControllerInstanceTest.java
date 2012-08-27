package unit.controllers;

import controllers.SupplierReports;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class SupplierReportControllerInstanceTest extends UnitTest{
    @Test
    public void controllerInstanceTest(){
        assertTrue(new SupplierReports() instanceof Controller);
    }
}
