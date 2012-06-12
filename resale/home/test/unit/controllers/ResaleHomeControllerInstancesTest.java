package unit.controllers;

import controllers.*;
import org.junit.Test;
import play.mvc.Controller;
import play.test.UnitTest;

/**
 * @author likang
 */
public class ResaleHomeControllerInstancesTest extends UnitTest{
    @Test
    public void controllerInstancesTest(){
        assertTrue(new Application() instanceof Controller);
        assertTrue(new Charge() instanceof Controller);
        assertTrue(new Coupons() instanceof Controller);
        assertTrue(new OrderResult() instanceof Controller);
        assertTrue(new PaymentInfo() instanceof Controller);
        assertTrue(new ResalerAccounts() instanceof Controller);
        assertTrue(new ResalerCarts() instanceof Controller);
        assertTrue(new ResalerFavs() instanceof Controller);
        assertTrue(new ResalerGoods() instanceof Controller);
        assertTrue(new ResalerInfos() instanceof Controller);
        assertTrue(new ResalerOrders() instanceof Controller);
        assertTrue(new ResalerPassword() instanceof Controller);
        assertTrue(new ResalerRegister() instanceof Controller);
        assertTrue(new ResalerWithdraw() instanceof Controller);
        assertTrue(new TaobaoAPIClient() instanceof Controller);
        assertTrue(new TaobaoOauthCallback() instanceof Controller);
    }
}
