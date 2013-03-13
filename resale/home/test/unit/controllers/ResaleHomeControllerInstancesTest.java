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
        //assertTrue(new WEBApplication() instanceof Controller);
        assertTrue(new Charge() instanceof Controller);
        assertTrue(new Coupons() instanceof Controller);
        assertTrue(new ResalerOrderResult() instanceof Controller);
        assertTrue(new ResalePaymentInfo() instanceof Controller);
        assertTrue(new ResalerAccounts() instanceof Controller);
        assertTrue(new ResalerCarts() instanceof Controller);
        assertTrue(new ResalerFavs() instanceof Controller);
        assertTrue(new ResalerGoods() instanceof Controller);
        assertTrue(new ResalerInfos() instanceof Controller);
        assertTrue(new ResalerOrders() instanceof Controller);
        assertTrue(new ResalerPassword() instanceof Controller);
        assertTrue(new ResalerRegister() instanceof Controller);
        assertTrue(new ResalerWithdraw() instanceof Controller);
        assertTrue(new TaobaoOauthCallback() instanceof Controller);
    }
}
