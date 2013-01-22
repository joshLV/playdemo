package unit.controllers;

import controllers.*;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class OperateBusinessControllerInstancesTest extends UnitTest {
    @Test
    public void controllerInstancesTest() {
        assertTrue(new OperateCategories() instanceof Controller);
        assertTrue(new Files() instanceof Controller);
        assertTrue(new OperateBrands() instanceof Controller);
        assertTrue(new OperateConsumers() instanceof Controller);
        assertTrue(new OperateCoupons() instanceof Controller);
        assertTrue(new OperateGoods() instanceof Controller);
        assertTrue(new OperateOrders() instanceof Controller);
        assertTrue(new OperateReports() instanceof Controller);
        assertTrue(new OperateShops() instanceof Controller);
        assertTrue(new Resalers() instanceof Controller);
        assertTrue(new Suppliers() instanceof Controller);
        assertTrue(new OperateUploadFiles() instanceof Controller);
        assertTrue(new WithdrawApproval() instanceof Controller);
        assertTrue(new Promotions() instanceof Controller);
        assertTrue(new OperateBusinessApplication() instanceof Controller);
        assertTrue(new AreasAdmin() instanceof Controller);
        assertTrue(new OperateDiscountCodes() instanceof Controller);
        assertTrue(new OperateConsumersWinningInfo() instanceof Controller);
    }
}
