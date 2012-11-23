package unit.controllers;

import org.junit.Test;

import play.mvc.Controller;
import play.test.UnitTest;
import controllers.Addresses;
import controllers.Areas;
import controllers.Carts;
import controllers.FindPassword;
import controllers.Goods;
import controllers.Home;
import controllers.OrderResult;
import controllers.Orders;
import controllers.PaymentInfo;
import controllers.PaymentNotify;
import controllers.Register;
import controllers.Votes;
import controllers.WEBApplication;

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
        assertTrue(new Goods() instanceof Controller);
        assertTrue(new Home() instanceof Controller);
        assertTrue(new OrderResult() instanceof Controller);
        assertTrue(new Orders() instanceof Controller);
        assertTrue(new PaymentInfo() instanceof Controller);
        assertTrue(new PaymentInfo() instanceof Controller);
        assertTrue(new PaymentNotify() instanceof Controller);
        assertTrue(new Register() instanceof Controller);
        assertTrue(new Votes() instanceof Controller);
    }
}
