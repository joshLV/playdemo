package controllers;

import controllers.modules.webtrace.WebTrace;
import models.consumer.*;
import models.order.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.util.*;

@With(WebTrace.class)
public class Carts extends Controller {

    public static void index() {
        String username = session.get("username");
        User user = User.find("byLoginName", username).first();
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
            if (cookieCarts != null) cartList.addAll(cookieCarts);
        }
        //合并结果集
        List<Cart> carts = new ArrayList<Cart>();
        if(cartList.size() > 0) {
            Map<Long,Cart> mapCarts = new HashMap<Long,Cart>();
            for(Cart cart : cartList) {
                Cart tmp = mapCarts.get(cart.goods.getId());
                if(tmp != null) {
                    tmp.number += cart.number;
                }else {
                    mapCarts.put(cart.goods.getId(), cart);
                    carts.add(cart);
                }
            }
        }
        String cartCookieId = cookieIdentity.value;
        render(carts, cartCookieId);
    }

    public static void order(long goodsId, int number) {
        String username = session.get("username");
        User user = User.find("byLoginName", username).first();
        Http.Cookie cookieIdentity = request.cookies.get("identity");

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        if ((user == null && cookieIdentity == null) || goods == null) {
            renderJSON(null);
        }

        Cart cart = null;
        if (user != null) {
            cart = Cart.find("byUserAndGoods", user, goods).first();
        } else {
            cart = Cart.find("byCookieIdentityAndGoods", cookieIdentity.value, goods).first();
        }

        if (cart != null) {
            if (cart.number != number) {
                cart.number += number;
                cart.materialType = goods.materialType;
                cart.save();
            }
        } else {
            if (user != null) {
                new Cart(user, null, goods, number, goods.materialType).save();
            } else {
                new Cart(user, cookieIdentity.value, goods, number, goods.materialType).save();
            }
        }

        renderJSON(cart);
    }

    public static void delete(long goodsId) {

        String username = session.get("username");
        User user = User.find("byLoginName", username).first();
        Http.Cookie cookieIdentity = request.cookies.get("identity");

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        if ((user == null && cookieIdentity == null) || goods == null) {
            renderJSON(null);
            return;
        }

        Cart cart = null;
        if (user != null) {
            cart = Cart.find("byUserAndGoods", user, goods).first();
        } else {
            cart = Cart.find("byCookieIdentityAndGoods", cookieIdentity.value, goods).first();
        }

        if (cart != null) {
            cart.delete();
        }

        renderJSON(null);
    }

    public static void batchDel(long[] goodsIds) {
        String username = session.get("username");
        User user = User.find("byLoginName", username).first();
        Http.Cookie cookieIdentity = request.cookies.get("identity");


        if (user == null && cookieIdentity == null) {
            renderJSON(null);
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


        renderJSON(null);
    }
}
