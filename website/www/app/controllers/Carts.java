package controllers;

import play.*;
import play.mvc.*;

import java.util.*;
import models.*;
import models.order.*;
import models.consumer.*;
import models.sales.*;

import controllers.modules.webtrace.WebTrace;

@With(WebTrace.class)
public class Carts extends Controller {


    public static void index() {
        String username = session.get("username");
        User user = User.find("byLoginName", username).first();
        Http.Cookie cookieIdentity = request.cookies.get("identity");
       
        List<Cart> carts = new ArrayList<Cart>();
        if (user != null ) {
            List<Cart> userCarts = Cart.find("byUser", user).fetch() ; 
            if (userCarts != null ) carts.addAll(userCarts);
        }
        if (cookieIdentity != null ) {
            List<Cart> cookieCarts = Cart.find("byCookieIdentity", cookieIdentity.value).fetch();
            if (cookieCarts != null ) carts.addAll(cookieCarts);
        }

        
        
        render();
    }
    
    public static void order(int goodsId, int number) {

        String username = session.get("username");
        User user = User.find("byLoginName", username).first();
        Http.Cookie cookieIdentity = request.cookies.get("identity");

        Goods goods = Goods.findById(goodsId);

        if ( (user == null && cookieIdentity == null) || goods == null){
            renderJSON(null);
            return ;
        }

        Cart cart = null;
        if (user != null) {
            cart = Cart.find("byUserAndGoods", user, goods).first();
        }else {
            cart = Cart.find("byCookieIdentityAndGoods", cookieIdentity.value,goods).first();
        }

        if (cart != null) {
            if (cart.number != number) {
                cart.number += number;
                cart.save();
            }
        }else {
            if (user != null) {
                new Cart(user, null, goods, number).save();
            }else {
                new Cart(user, cookieIdentity.value, goods, number).save();
            }
        }

        renderJSON(null);
    }

    public static void delete(int goodsId) {

        String username = session.get("username");
        User user = User.find("byLoginName", username).first();
        Http.Cookie cookieIdentity = request.cookies.get("identity");

        Goods goods = Goods.findById(goodsId);

        if ( (user == null && cookieIdentity == null) || goods == null){
            renderJSON(null);
            return ;
        }

        Cart cart = null;
        if (user != null) {
            cart = Cart.find("byUserAndGoods", user, goods).first();
        }else {
            cart = Cart.find("byCookieIdentityAndGoods", cookieIdentity.value,goods).first();
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


        if ( user == null && cookieIdentity == null){
            renderJSON(null);
            return ;
        }

        Cart cart = null;
        if (user != null) {
            for (long goodsId : goodsIds) {
                Goods goods = Goods.findById(goodsId);
                cart = Cart.find("byUserAndGoods", user, goods).first();
                if (cart != null) {
                    cart.delete();
                }
            }
        }else {
            for (long goodsId : goodsIds) {
                Goods goods = Goods.findById(goodsId);
                cart = Cart.find("byCookieIdentityAndGoods", cookieIdentity.value,goods).first();
                if (cart != null) {
                    cart.delete();
                }
            }
        }


        renderJSON(null);
    }

}
