package unit.controllers;

import controllers.Addresses;
import controllers.Areas;
import controllers.Carts;
import controllers.FindPassword;
import controllers.Goods2;
import controllers.Home2;
import controllers.OrderResult;
import controllers.Orders;
import controllers.PaymentInfo;
import controllers.PaymentNotify;
import controllers.Register;
import controllers.Votes;
import controllers.WEBApplication;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class ControllerInstancesTest extends UnitTest{

    @Test
    public void controllerInstancesTest(){
        assertTrue(new Addresses() instanceof Controller);
        assertTrue(new WEBApplication() instanceof Controller);
        assertTrue(new Areas() instanceof Controller);
        assertTrue(new Carts() instanceof Controller);
        assertTrue(new FindPassword() instanceof Controller);
        assertTrue(new Goods2() instanceof Controller);
        assertTrue(new Home2() instanceof Controller);
        assertTrue(new OrderResult() instanceof Controller);
        assertTrue(new Orders() instanceof Controller);
        assertTrue(new PaymentInfo() instanceof Controller);
        assertTrue(new PaymentInfo() instanceof Controller);
        assertTrue(new PaymentNotify() instanceof Controller);
        assertTrue(new Register() instanceof Controller);
        assertTrue(new Votes() instanceof Controller);
    }
}
