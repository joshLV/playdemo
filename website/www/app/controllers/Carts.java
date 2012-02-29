package controllers;

import controllers.modules.webcas.WebCAS;
import models.consumer.User;
import models.order.Cart;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.util.List;

/**
 * 购物车控制器，提供http接口对购物车进行增删该查
 *
 * @author likang
 *
 */
@With(WebCAS.class)
public class Carts extends Controller {

    /**
     * 购物车主界面
     */
    public static void index() {
        User user = WebCAS.getUser();
        Http.Cookie cookie= request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        List<Cart> carts = Cart.findAll(user, cookieValue);
        render(carts);
    }

    /**
     * 加入或修改购物车列表
     *
     * @param goodsId  商品ID
     * @param increment 购物车中商品数增量，
     * 若购物车中无此商品，则新建条目
     * 若购物车中有此商品，且商品数量加增量小于等于0，视为无效
     */
    public static void order(long goodsId, int increment) {
        User user = WebCAS.getUser();
        Http.Cookie cookie= request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        if (goods == null) {
            error(500, "no such goods: " + goodsId);
            return;
        }
        if (user == null && cookie == null) {
            error(500, "can not identity current user");
            return;
        }
        
        Cart cart = Cart.order(user, cookieValue, goods, increment);

        ok();
    }

    /**
     * 以JSON格式展示所有购物车内容
     */
    public static void allJSON() {
        User user = WebCAS.getUser();
        Http.Cookie cookie= request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        List<Cart> carts = Cart.findAll(user, cookieValue);
        render(carts);
    }

    /**
     * 从购物车中删除指定商品列表
     *
     * @param goodsIds 商品列表
     */
    public static void delete(@As(",") List<Long> goodsIds) {
        User user = WebCAS.getUser();
        Http.Cookie cookie= request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        if (user == null && cookie == null) {
            error(500, "can not identity current user");
            return;
        }
        if (goodsIds == null || goodsIds.size() == 0){
            error(500, "no goods specified");
            return;
        }

        Cart.delete(user, cookieValue, goodsIds);

        ok();
    }
}
