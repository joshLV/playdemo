package controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import controllers.modules.cas.SecureCAS;
import controllers.resaletrace.ResaleCAS;

import models.accounts.AccountType;
import models.consumer.User;

import models.order.Cart;
import models.order.Order;
import models.order.OrderItems;
import models.resale.Resaler;
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
@With({ResaleCAS.class, SecureCAS.class})
public class ResalerCarts extends Controller {

    /**
     * 购物车主界面
     */
    public static void index() {
        Resaler resaler = ResaleCAS.getResaler();

        List<ResalerFav> favs = ResalerFav.findAll(resaler);

        List<List<ResalerCart>> carts = ResalerCart.groupFindAll(resaler);
        render(carts,favs, resaler);
    }
    
    public static void showCarts(){
        Resaler resaler = ResaleCAS.getResaler();
        List<List<ResalerCart>> carts = ResalerCart.groupFindAll(resaler);
        render(carts, resaler);
    }
    
    public static void confirmCarts(){
        Resaler resaler = ResaleCAS.getResaler();
        List<ResalerCart> carts = ResalerCart.findAll(resaler);
        
        Order order = new Order(resaler.getId(), AccountType.RESALER, null);
        order.save();
        
        BigDecimal amount = new BigDecimal(0);
        for(ResalerCart cart : carts){
            OrderItems orderItems = new OrderItems(order, cart.goods, cart.number, cart.phone);
            orderItems.resalerPrice = cart.goods.getResalePrice(resaler.level);
            orderItems.save();
            amount = amount.add(cart.goods.salePrice);
        }
        
        order.amount = amount;
        order.needPay = amount;        
        order.save();
        
        ResalerCart.clear(resaler);
        
        redirect("/payment_info/" + order.getId());
    }

    /**
     * 批量加入购物车
     *
     * @param goodsId  商品ID
     * @param phones   手机号列表，以回车或者空格等空白字符分割
     */
    public static void formAdd(long goodsId, String phones) {
        Resaler resaler = ResaleCAS.getResaler();
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        List<String> phoneLines = Arrays.asList(phones.split("\\s+"));

        List<String> invalidPhones = ResalerCart.batchOrder(resaler, goods, phoneLines);

        if (invalidPhones == null) {
            renderArgs.put("errorMsg", "invalid input");
        }else {
            renderArgs.put("invalidPhones", invalidPhones);
        }
        
        index();
    }

    /**
     * 加入或修改购物车列表
     *
     * @param goodsId  商品ID
     * @param phones   手机号
     * @param increment 购物车中商品数增量，
     * 若购物车中无此商品，则新建条目
     * 若购物车中有此商品，且商品数量加增量小于等于0，视为无效
     */
    public static void reorder(long goodsId, String phone, int increment) {
        Resaler resaler = ResaleCAS.getResaler();
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        ResalerCart resalerCart = ResalerCart.reorder(resaler, goods, phone, increment);

        if (resalerCart == null) {
            error(500, "invalid input");
        }else {
            ok();
        }
    }

    /**
     * 从购物车中删除指定商品列表，并返回购物车界面
     *
     * @param goodsIds 商品列表
     */
    public static void formBatchDelete(List<Long> goodsIds ) {
        Resaler resaler = ResaleCAS.getResaler();
        for (long goodsId : goodsIds) {
            models.sales.Goods goods = models.sales.Goods.findById(goodsId);
            ResalerCart.delete(resaler, goods );
        }

        index();
    }

    /**
     * 从购物车中删除指定商品列表，并返回购物车界面
     *
     * @param goodsId  商品
     * @param phone    手机号
     */
    public static void formDelete(long goodsId, String phone) {
        Resaler resaler = ResaleCAS.getResaler();
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        ResalerCart result = ResalerCart.delete(resaler, goods, phone);

        index();
    }

    /**
     * 从购物车中删除指定商品列表
     *
     * @param goodsId  商品
     * @param phone    手机号
     */
    public static void delete(long goodsId, String phone) {
        Resaler resaler = ResaleCAS.getResaler();
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        ResalerCart result = ResalerCart.delete(resaler, goods, phone);

        if (result == null ){
            error(500, "invalid input");
        }else{
            ok();
        }
    }
}
