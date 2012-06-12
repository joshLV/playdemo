package unit.controllers;

import controllers.*;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class WebsiteHomeControllerInstanceTest extends UnitTest{
    @Test
    public void controllerInstanceTest(){
        assertTrue(new UserAddresses() instanceof Controller);
        assertTrue(new UserCarts() instanceof Controller);
        assertTrue(new UserCoupons() instanceof Controller);
        assertTrue(new UserInfos() instanceof Controller);
        assertTrue(new UserOrders() instanceof Controller);
        assertTrue(new UserPasswords() instanceof Controller);
        assertTrue(new UserPoints() instanceof Controller);
        assertTrue(new UserSequences() instanceof Controller);
        assertTrue(new UserWithdraw() instanceof Controller);
    }
}
