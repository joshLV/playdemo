package controllers;

import java.util.ArrayList;
import java.util.Map;

import java.util.regex.Pattern;

import controllers.modules.webcas.WebCAS;
import models.consumer.User;

import models.order.Cart;
import models.resale.ResalerCart;
import models.resale.ResalerFav;

import models.sales.Goods;
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
//@With({WebCAS.class,SecureCAS.class})
@With(WebCAS.class)
public class ResalerCarts extends Controller {
    private static Pattern phonePattern = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$"); 

    /**
     * 购物车主界面
     */
    public static void index() {
        User user = WebCAS.getUser();

        List<ResalerFav> favs = ResalerFav.findAll(user);

        List<List<ResalerCart>> carts = ResalerCart.findAll(user);
        render(carts,favs);
    }

    /**
     * 加入或修改购物车列表
     *
     * @param goodsId  商品ID
     * @param increment 购物车中商品数增量，
     * 若购物车中无此商品，则新建条目
     * 若购物车中有此商品，且商品数量加增量小于等于0，视为无效
     */
    public static void order(long goodsId, String phones, int increment) {
        User user = WebCAS.getUser();
        List<String> invalidPhones = new ArrayList<>();
        List<String> validPhones = new ArrayList<>();
        String[] phoneLines = phones.split("\\s+");
        for(String p : phoneLines){
            if(!phonePattern.matcher(p).matches()){
                invalidPhones.add(p);
                continue;
            }
            validPhones.add(p);
        }

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        if (goods == null) {
            error(500, "no such goods: " + goodsId);
        }
        
        for(String p : validPhones){
            ResalerCart.order(user, goods, p, increment);
        }

        renderArgs.put("validPhones", validPhones);
        index();
    }

    /**
     * 从购物车中删除指定商品列表
     *
     * @param goodsIds 商品列表
     * @param phone    手机号
     */
    public static void delete(long goodsId, String phone) {
        User user = WebCAS.getUser();

        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        int result = ResalerCart.delete(user, goods, phone);
        renderJSON(result);
    }
}
