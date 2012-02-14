package controllers;

import controllers.modules.webtrace.WebTrace;
import models.consumer.address.Address;
import models.order.Cart;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 用户订单确认控制器.
 * <p/>
 * User: sujie
 * Date: 2/14/12
 * Time: 11:31 AM
 */
@With(WebTrace.class)
public class Orders extends Controller {

    public static void index() {
        List<Address> addressList = Address.findByOrder();

        List<Cart> eCartList = Cart.findECart();
        float eCartAmount = amount(eCartList);


        List<Cart> rCartList = Cart.findRCart();
        float rCartAmount = amount(rCartList) + 5;

        render(addressList, eCartList, eCartAmount, rCartList, rCartAmount);
    }

    public static void index(String cartCookieId) {
        List<Address> addressList = Address.findByOrder();

        List<Cart> eCartList = Cart.findECart(cartCookieId);
        float eCartAmount = amount(eCartList);


        List<Cart> rCartList = Cart.findRCart(cartCookieId);
        float rCartAmount = amount(rCartList) + 5;

        render(addressList, eCartList, eCartAmount, rCartList, rCartAmount);
    }

    private static float amount(List<Cart> cartList) {
        float cartAmount = 0;
        for (Cart cart : cartList) {
            cartAmount += cart.goods.sale_price * cart.number;
        }
        return cartAmount;
    }

}