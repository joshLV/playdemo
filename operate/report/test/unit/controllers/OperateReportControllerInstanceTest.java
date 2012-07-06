package unit.controllers;

import controllers.Application;
import controllers.PurchaseTaxReports;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class OperateReportControllerInstanceTest extends UnitTest{
    @Test
    public void controllerInstanceTest(){
        assertTrue(new Application() instanceof Controller);
        assertTrue(new PurchaseTaxReports() instanceof Controller);
    }
}
