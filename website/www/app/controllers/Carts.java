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
import javax.persistence.*;

@With(WebCAS.class)
public class Carts extends Controller {

    public static void index() {
        User user = WebCAS.getUser();
        Http.Cookie cookieIdentity = request.cookies.get("identity");

        List<Cart> carts = Cart.findAll(user, cookieIdentity.value);
        render(carts);
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
                new Cart(user, null, goods, number).save();
            } else {
                new Cart(user, cookieIdentity.value, goods, number).save();
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
        }
        for (long goodsId : goodsIds) {
            models.sales.Goods goods = models.sales.Goods.findById(goodsId);
            cart = Cart.find("byCookieIdentityAndGoods", cookieIdentity.value, goods).first();
            if (cart != null) {
                cart.delete();
            }
        }

        ok();
    }
}
