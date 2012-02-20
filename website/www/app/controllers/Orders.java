package controllers;

import controllers.modules.webtrace.WebTrace;
import models.consumer.Address;
import models.order.Cart;
import play.mvc.Http;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户订单确认控制器.
 * <p/>
 * User: sujie
 * Date: 2/14/12
 * Time: 11:31 AM
 */
@With(WebTrace.class)
public class Orders extends AbstractLoginController {

    /**
     * 订单确认.
     */
    public static void index() {
        List<Address> addressList = Address.findByOrder();

        boolean buyNow = Boolean.parseBoolean(session.get("buyNow"));
        if (buyNow) {//立即购买，则不从购物车取购买的商品信息，而直接从session中获取
            List<Cart> eCartList = new ArrayList<>();
            BigDecimal eCartAmount = new BigDecimal(0);
            List<Cart> rCartList = new ArrayList<>();
            BigDecimal rCartAmount = new BigDecimal(0);
            long goodsId = Long.parseLong(session.get("goodsId"));
            int number = Integer.parseInt(session.get("number"));
            models.sales.Goods goods = models.sales.Goods.findById(goodsId);
            Cart cart = new Cart(getUser(), null, goods, number, goods.materialType);

            switch (goods.materialType) {
                case Electronic:
                    eCartList.add(cart);
                    eCartAmount = amount(eCartList);
                    break;
                case Real:
                    rCartList.add(cart);
                    rCartAmount = amount(rCartList).add(new BigDecimal(5));
                    break;
            }
            render(addressList, eCartList, eCartAmount, rCartList, rCartAmount);
        }
        //从购物车结算购买
        Http.Cookie cookieIdentity = request.cookies.get("identity");

        List<Cart> eCartList = Cart.findECart(cookieIdentity.value);
        BigDecimal eCartAmount = amount(eCartList);


        List<Cart> rCartList = Cart.findRCart(cookieIdentity.value);
        BigDecimal rCartAmount = amount(rCartList).add(new BigDecimal(5));

        render(addressList, eCartList, eCartAmount, rCartList, rCartAmount);
    }

    private static BigDecimal amount(List<Cart> cartList) {
        BigDecimal cartAmount = new BigDecimal(0);
        for (Cart cart : cartList) {
            cartAmount = cart.goods.salePrice.multiply(new BigDecimal(cart.number)).add(cartAmount);
        }
        return cartAmount;
    }

    /**
     * 立即购买操作.
     *
     * @param goodsId   购买商品
     * @param number    购买数量
     */
    public static void buy(long goodsId, int number) {
        session.put("buyNow", true);
        session.put("goodsId", goodsId);
        session.put("number", number);
        redirect("/orders");
    }

    /**
     * 提交订单.
     */
    public static void create() {

    }
}