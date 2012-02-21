package controllers;

import controllers.modules.webtrace.WebTrace;
import controllers.modules.cas.*;
import models.consumer.Address;
import models.order.Cart;
import models.order.NotEnoughInventoryException;
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
//@With(SecureCAS.class)
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
            long number = Long.parseLong(session.get("number"));
            models.sales.Goods goods = models.sales.Goods.findById(goodsId);
            Cart cart = new Cart(getUser(), null, goods, number, goods.materialType);

            switch (goods.materialType) {
            case Electronic:
                eCartList.add(cart);
                eCartAmount = Cart.amount(eCartList);
                break;
            case Real:
                rCartList.add(cart);
                rCartAmount = Cart.amount(rCartList).add(new BigDecimal(5));
                break;
            }
            render(addressList, eCartList, eCartAmount, rCartList, rCartAmount);
        }
        //从购物车结算购买
        Http.Cookie cookieIdentity = request.cookies.get("identity");

        List<Cart> eCartList = Cart.findECart(cookieIdentity.value);
        BigDecimal eCartAmount = Cart.amount(eCartList);


        List<Cart> rCartList = Cart.findRCart(cookieIdentity.value);
        BigDecimal rCartAmount = Cart.amount(rCartList).add(new BigDecimal(5));

        render(addressList, eCartList, eCartAmount, rCartList, rCartAmount);
    }

    /**
     * 立即购买操作.
     *
     * @param goodsId 购买商品
     * @param number  购买数量
     */
    public static void buy(long goodsId, long number) {
        session.put("buyNow", true);
        session.put("goodsId", goodsId);
        session.put("number", number);
        redirect("/orders");
    }

    /**
     * 提交订单.
     */
    public static void create() {
        System.out.println("create null");
        create0(null);
    }

    /**
     * 提交订单.
     */
    public static void create(String mobile) {
        System.out.println("create mobile=" + mobile);
        create0(mobile);
    }

    private static void create0(String mobile) {
        boolean buyNow = Boolean.parseBoolean(session.get("buyNow"));
        Address defaultAddress = Address.findDefault(getUser());
        models.order.Orders orders;
        try {
            System.out.println("buyNow=" + buyNow);
            if (buyNow) {
                long goodsId = Long.parseLong(session.get("goodsId"));
                long number = Integer.parseInt(session.get("number"));
                orders = new models.order.Orders(getUser(), goodsId, number, defaultAddress, mobile);

            } else {
                Http.Cookie cookieIdentity = request.cookies.get("identity");

                List<Cart> eCartList = Cart.findByCookie(cookieIdentity.value);
                orders = new models.order.Orders(getUser(), eCartList, defaultAddress);
            }
            orders.save();
            session.put("buyNow", false);
            redirect("/payment_info/" + orders.id);
        } catch (NotEnoughInventoryException e) {
            System.out.println(e);
            //todo 缺少库存

        }
    }
}
