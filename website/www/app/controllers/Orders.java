package controllers;

import controllers.modules.cas.*;
import controllers.modules.webcas.WebCAS;
import models.consumer.Address;
import models.order.Cart;
import models.order.NotEnoughInventoryException;
import play.data.validation.Min;
import play.data.validation.Required;
import play.mvc.Controller;
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
@With({SecureCAS.class, WebCAS.class})
public class Orders extends Controller {
    /**
     * 订单确认.
     */
    public static void index() {

        List<Address> addressList = Address.findByOrder(WebCAS.getUser());

        boolean buyNow = Boolean.parseBoolean(session.get("buyNow"));
        if (buyNow) {//立即购买，则不从购物车取购买的商品信息，而直接从session中获取
            List<Cart> eCartList = new ArrayList<>();
            BigDecimal eCartAmount = new BigDecimal(0);
            List<Cart> rCartList = new ArrayList<>();
            BigDecimal rCartAmount = new BigDecimal(0);
            long goodsId = Long.parseLong(session.get("goodsId"));
            long number = Long.parseLong(session.get("number"));
            models.sales.Goods goods = models.sales.Goods.findById(goodsId);
            Cart cart = new Cart(WebCAS.getUser(), null, goods, number, goods.materialType);

            switch (goods.materialType) {
                case ELECTRONIC:
                    eCartList.add(cart);
                    eCartAmount = Cart.amount(eCartList);
                    renderArgs.put("goodsAmount", eCartAmount);
                    renderArgs.put("totalAmount", eCartAmount);
                    break;
                case REAL:
                    rCartList.add(cart);
                    BigDecimal goodsAmount = Cart.amount(rCartList);
                    rCartAmount = goodsAmount.add(new BigDecimal(5));
                    renderArgs.put("goodsAmount", goodsAmount);
                    renderArgs.put("totalAmount", rCartAmount);
                    break;
            }
            render(addressList, eCartList, eCartAmount, rCartList, rCartAmount);
        }
        //从购物车结算购买
        List<Cart> eCartList = Cart.findECart(WebCAS.getUser());
        BigDecimal eCartAmount = Cart.amount(eCartList);


        List<Cart> rCartList = Cart.findRCart(WebCAS.getUser());
        BigDecimal rCartAmount;
        if (rCartList.size() == 0) {
            rCartAmount = new BigDecimal(0);
        } else {
            rCartAmount = Cart.amount(rCartList).add(new BigDecimal(5));
        }
        BigDecimal totalAmount = eCartAmount.add(rCartAmount);
        BigDecimal goodsAmount = rCartList.size() == 0 ? eCartAmount : totalAmount.subtract(new BigDecimal(5));

        renderArgs.put("goodsAmount", goodsAmount);
        renderArgs.put("totalAmount", totalAmount);
        render(addressList, eCartList, eCartAmount, rCartList, rCartAmount);
    }

    /**
     * 立即购买操作.
     *
     * @param goodsId 购买商品
     * @param number  购买数量
     */
    public static void buy(@Required long goodsId, @Required(message = "购买数量应大于0") @Min(value = 1, message = "购买数量应大于或等于0") long number) {
        if (validation.hasErrors()) {
            params.flash(); // add http parameters to the flash scope
            validation.keep(); // keep the errors for the next request
            Goods.show(goodsId);
        }
        session.put("buyNow", true);
        session.put("goodsId", goodsId);
        session.put("number", number);
        redirect("/orders");
    }

    /**
     * 提交订单.
     */
    public static void create() {
        create0(null);
    }

    /**
     * 提交订单.
     */
    public static void create(String mobile) {
        create0(mobile);
    }

    private static void create0(String mobile) {
        Http.Cookie cookieIdentity = request.cookies.get("identity");
        boolean buyNow = Boolean.parseBoolean(session.get("buyNow"));
        Address defaultAddress = Address.findDefault(WebCAS.getUser());
        models.order.Orders orders;
        try {
            if (buyNow) {
                long goodsId = Long.parseLong(session.get("goodsId"));
                long number = Integer.parseInt(session.get("number"));
                orders = new models.order.Orders(WebCAS.getUser(), goodsId, number, defaultAddress, mobile);

            } else {

                List<Cart> eCartList = Cart.findByUser(WebCAS.getUser());
                orders = new models.order.Orders(WebCAS.getUser(), eCartList, defaultAddress);
            }
            orders.createAndUpdateInventory(WebCAS.getUser(), cookieIdentity.value);

            session.put("buyNow", false);
            redirect("/payment_info/" + orders.id);
        } catch (NotEnoughInventoryException e) {
            //todo 缺少库存
            System.out.println(e);


        }
    }
}
