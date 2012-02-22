package controllers;

import controllers.modules.webcas.WebCAS;
import models.consumer.User;
import models.order.Cart;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@With(WebCAS.class)
public class Carts extends Controller {

    public static void index() {
        User user = WebCAS.getUser();
        Http.Cookie cookieIdentity = request.cookies.get("identity");

        //查询登陆用户已保存的购物车
        List<Cart> cartList = new ArrayList<Cart>();
        if (user != null) {
            List<Cart> userCarts = Cart.find("byUser", user).fetch();
            cartList.addAll(userCarts);
        }
        //查询未登陆情况下已保存的购物车
        if (cookieIdentity != null) {
            List<Cart> cookieCarts = Cart.find("byCookieIdentity", cookieIdentity.value).fetch();
            cartList.addAll(cookieCarts);
        }
        //合并结果集
        List<Cart> cartMergeList = new ArrayList<Cart>();
        if (cartList.size() > 0) {
            Map<Long, Cart> mapCarts = new HashMap<Long, Cart>();
            for (Cart cart : cartList) {
                Cart tmp = mapCarts.get(cart.goods.getId());
                if (tmp != null) {
                    tmp.number += cart.number;
                } else {
                    mapCarts.put(cart.goods.getId(), cart);
                    cartMergeList.add(cart);
                }
            }
        }
        render(cartMergeList);
    }

    public static void order(long goodsId, int number) {
        User user = WebCAS.getUser();
        Http.Cookie cookieIdentity = request.cookies.get("identity");

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        if ((user == null && cookieIdentity == null) || goods == null) {
            error(500, "can not identity user");
        }

        Cart cart;
        if (user != null) {
            cart = Cart.find("byUserAndGoods", user, goods).first();
        } else {
            cart = Cart.find("byCookieIdentityAndGoods", cookieIdentity.value, goods).first();
        }

        if (cart != null) {
            if (cart.number + number >= 0) {
                cart.number += number;
                cart.save();
            }
        } else {
            if (user != null) {
                new Cart(user, null, goods, number, goods.materialType).save();
            } else {
                new Cart(user, cookieIdentity.value, goods, number, goods.materialType).save();
            }
        }

        ok();
    }

    public static void delete(@As(",") List<Long> goodsIds) {
        User user = WebCAS.getUser();
        Http.Cookie cookieIdentity = request.cookies.get("identity");


        if (user == null && cookieIdentity == null) {
            error(500, "can not identity user");
            return;
        }

        Cart cart = null;
        if (user != null) {
            for (long goodsId : goodsIds) {
                models.sales.Goods goods = models.sales.Goods.findById(goodsId);
                cart = Cart.find("byUserAndGoods", user, goods).first();
                if (cart != null) {
                    cart.delete();
                }
            }
        } else {
            for (long goodsId : goodsIds) {
                models.sales.Goods goods = models.sales.Goods.findById(goodsId);
                cart = Cart.find("byCookieIdentityAndGoods", cookieIdentity.value, goods).first();
                if (cart != null) {
                    cart.delete();
                }
            }
        }


        ok();
    }
}
