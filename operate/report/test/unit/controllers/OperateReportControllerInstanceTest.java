package unit.controllers;

import controllers.OperateReportApplication;
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
        assertTrue(new OperateReportApplication() instanceof Controller);
        assertTrue(new PurchaseTaxReports() instanceof Controller);
    }
}
