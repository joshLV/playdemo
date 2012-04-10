package controllers;

import models.accounts.AccountType;
import models.consumer.Address;
import models.consumer.User;
import models.order.Cart;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import play.Logger;
import play.data.validation.Min;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;

import java.util.HashMap;
import java.util.Map;
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
@With(SecureCAS.class)
public class Orders extends Controller {
    /**
     * 预览订单.
     * @param items 选择的商品及数量，格式为 goods1-num1,goods2-num2-.....
     */
    public static void index(String items) {
        if(items == null){
            error("no goods specified");
            return;
        }
        //解析提交的商品及数量
        List<Long> goodsIds = new ArrayList<>();
        Map<Long,Integer> itemsMap = new HashMap<>();
        parseItems(items, goodsIds, itemsMap);
        
        //计算电子商品列表和非电子商品列表
        List<Cart> eCartList = new ArrayList<>();
        BigDecimal eCartAmount = BigDecimal.ZERO;
        List<Cart> rCartList = new ArrayList<>();
        BigDecimal rCartAmount = BigDecimal.ZERO;
        
        List<models.sales.Goods> goods = models.sales.Goods.findInIdList(goodsIds);
        for (models.sales.Goods g : goods){
            Integer number = itemsMap.get(g.getId());
            if(g.materialType == models.sales.MaterialType.REAL){
                rCartList.add(new Cart(g, number));
                rCartAmount = rCartAmount.add(g.salePrice.multiply(new BigDecimal(number.toString())));
            }else if(g.materialType == models.sales.MaterialType.ELECTRONIC){
                eCartList.add(new Cart(g, number));
                eCartAmount = eCartAmount.add(g.salePrice.multiply(new BigDecimal(number.toString())));
            }
        }

        if(rCartList.size() == 0 && eCartList.size() == 0){
            error("no goods specified");
            return;
        }
        
        List<Address> addressList = Address.findByOrder(SecureCAS.getUser());

        //如果有实物商品，加上运费
         if (rCartList.size() > 0) {
            rCartAmount = rCartAmount.add(new BigDecimal("5"));
        }
        BigDecimal totalAmount = eCartAmount.add(rCartAmount);
        BigDecimal goodsAmount = rCartList.size() == 0 ? eCartAmount : totalAmount.subtract(new BigDecimal("5"));

        renderArgs.put("goodsAmount", goodsAmount);
        renderArgs.put("totalAmount", totalAmount);
        render(addressList, eCartList, eCartAmount, rCartList, rCartAmount, items);
    }
    
    /**
     * 提交订单.
     */
    public static void create(String items, String mobile){
        Http.Cookie cookie= request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;
        User user = SecureCAS.getUser();

        //解析提交的商品及数量
        List<Long> goodsIds = new ArrayList<>();
        Map<Long,Integer> itemsMap = new HashMap<>();
        parseItems(items, goodsIds, itemsMap);

        //创建订单
        Order order = new Order(user.getId(), AccountType.CONSUMER);
        Address defaultAddress = Address.findDefault(user);
        order.setAddress(defaultAddress);

        List<models.sales.Goods> goods = models.sales.Goods.findInIdList(goodsIds);
        //添加订单条目
        try{
            for(models.sales.Goods goodsItem : goods){
                order.addOrderItem(goodsItem, itemsMap.get(goodsItem.getId()), mobile);
            }
        }catch (NotEnoughInventoryException e){
            //todo 缺少库存
            Logger.error(e, "inventory not enough");
            error("inventory not enough");
        }
        //确认订单
        order.createAndUpdateInventory();
        //删除购物车中相应物品
        Cart.delete(user, cookieValue, goodsIds);
        
        redirect("/payment_info/" + order.getId());
    }
    
    private static void parseItems(String items, List<Long> goodsIds, Map<Long, Integer> itemsMap){
        String[] itemSplits = items.split(",");
        for(String split : itemSplits){
            String[] goodsItem = split.split("-");
            if(goodsItem.length == 2){
                Integer number = Integer.parseInt(goodsItem[1]);
                if(number > 0){
                    Long goodsId = Long.parseLong(goodsItem[0]);
                    goodsIds.add(goodsId);
                    itemsMap.put(goodsId, number);
                }
            }
        }
    }
}
