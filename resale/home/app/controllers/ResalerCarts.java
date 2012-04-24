package controllers;

import java.math.BigDecimal;
import java.util.*;

import models.accounts.AccountType;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderItems;
import models.resale.Resaler;
import models.resale.ResalerCart;
import models.resale.ResalerFav;
import models.sales.Goods;
import models.sales.MaterialType;
import play.mvc.Controller;
import play.mvc.With;
import controllers.modules.resale.cas.SecureCAS;

/**
 * 购物车控制器，提供http接口对购物车进行增删该查
 *
 * @author likang
 *
 */
@With(SecureCAS.class)
public class ResalerCarts extends Controller {

    /**
     * 购物车主界面
     */
    public static void index() {
        Resaler resaler = SecureCAS.getResaler();

        List<ResalerFav> favs = ResalerFav.findAll(resaler);

        List<List<ResalerCart>> carts = ResalerCart.groupFindAll(resaler);
        render(carts,favs, resaler);
    }
    
    public static void showCarts(){
        Resaler resaler = SecureCAS.getResaler();
        List<List<ResalerCart>> carts = ResalerCart.groupFindAll(resaler);
        render(carts, resaler);
    }
    
    public static void confirmCarts(String favItems) throws NotEnoughInventoryException{
        Resaler resaler = SecureCAS.getResaler();

        //解析提交的商品及数量
        List<Object[]> favs =  parseItems(favItems);

        if(favs.size() == 0){
            error("购物车中无商品");
            return;
        }

        Order order = new Order(resaler.getId(), AccountType.RESALER);
        for(Object[] fav : favs){
            Goods goods = (Goods)fav[0];
            int number = (Integer)fav[1];
            String phone = (String)fav[2];
            BigDecimal resalerPrice = goods.getResalePrice(resaler.level);
            order.addOrderItem(goods, number, phone,
                    resalerPrice, // 分销商成本价即成交价
                    resalerPrice  // 分销商成本价
            );
            ResalerCart.delete(resaler, goods, phone);

        }

        order.createAndUpdateInventory();
        redirect("/payment_info/" + order.getId());
    }

    /**
     * 批量加入购物车
     *
     * @param goodsId  商品ID
     * @param phones   手机号列表，以回车或者空格等空白字符分割
     */
    public static void formAdd(long goodsId, String phones) {
        Resaler resaler = SecureCAS.getResaler();
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
     * @param phone    手机号
     * @param increment 购物车中商品数增量，
     * 若购物车中无此商品，则新建条目
     * 若购物车中有此商品，且商品数量加增量小于等于0，视为无效
     */
    public static void reorder(long goodsId, String phone, int increment) {
        Resaler resaler = SecureCAS.getResaler();
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
        Resaler resaler = SecureCAS.getResaler();
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
        Resaler resaler = SecureCAS.getResaler();
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        // FIXME: result 没有被使用
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
        Resaler resaler = SecureCAS.getResaler();
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);

        ResalerCart result = ResalerCart.delete(resaler, goods, phone);

        if (result == null ){
            error(500, "invalid input");
        }else{
            ok();
        }
    }
    private static List<Object[]> parseItems(String items){
        String[] itemSplits = items.split(",");
        List<Object[]> parsedItems = new ArrayList<>();
        for(String split : itemSplits){
            String[] goodsItem = split.split("-");
            if(goodsItem.length == 3){
                Integer number = Integer.parseInt(goodsItem[1]);
                if(number > 0){
                    Long goodsId = Long.parseLong(goodsItem[0]);
                    Goods goods = Goods.findById(goodsId);
                    parsedItems.add(new Object[]{goods, number, goodsItem[2]});
                }
            }
        }
        return parsedItems;
    }
}
