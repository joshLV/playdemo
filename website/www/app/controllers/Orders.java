package controllers;

import controllers.modules.webtrace.WebTrace;
import models.consumer.Address;
import models.order.Cart;
import play.mvc.Controller;
import play.mvc.Http;
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

        Http.Cookie cookieIdentity = request.cookies.get("identity");
        //todo 处理cookieIdentity为空的时候，即立即购买的

        List<Cart> eCartList = Cart.findECart(cookieIdentity.value);
        float eCartAmount = amount(eCartList);


        List<Cart> rCartList = Cart.findRCart(cookieIdentity.value);
        float rCartAmount = amount(rCartList) + 5;

        render(addressList, eCartList, eCartAmount, rCartList, rCartAmount);
    }

    private static float amount(List<Cart> cartList) {
        float cartAmount = 0;
        for (Cart cart : cartList) {
            cartAmount += cart.goods.salePrice * cart.number;
        }
        return cartAmount;
    }

}