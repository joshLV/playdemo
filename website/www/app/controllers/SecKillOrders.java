package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.accounts.AccountType;
import models.consumer.Address;
import models.consumer.User;
import models.order.Cart;
import models.order.DeliveryType;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OrderItems;
import models.sales.MaterialType;
import models.sales.SecKillGoodsItem;
import play.Logger;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static play.Logger.warn;

/**
 * 秒杀商品订单.
 * <p/>
 * User: sujie
 * Date: 8/15/12
 * Time: 11:41 AM
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class SecKillOrders extends Controller {
    /**
     * 预览订单
     */
    public static void index(long secKillGoodsItemId) {
        showOrder(secKillGoodsItemId);

        User user = SecureCAS.getUser();
        List<String> orderItemsMobiles = OrderItems.getMobiles(user);

        render(user, orderItemsMobiles);
    }

    /**
     * 创建秒杀商品的订单.
     */
    public static void create(long secKillGoodsItemId,  String mobile, String remark) {
        System.out.println("secKillGoodsItemId:" + secKillGoodsItemId);

        long count = 1;
        //如果订单中有电子券，则必须填写手机号
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;
        User user = SecureCAS.getUser();

        //解析提交的商品及数量
        SecKillGoodsItem secKillGoodsItem = SecKillGoodsItem.findById(secKillGoodsItemId);

        //判断帐号限购
        boolean exceedLimit = checkLimitNumber(user, secKillGoodsItem.secKillGoods.goods.id, secKillGoodsItemId, count);
        if (exceedLimit) {
            render("SecKillGoods/index.html", exceedLimit);
        }

        boolean isElectronic = secKillGoodsItem.secKillGoods.goods.materialType.equals(MaterialType.ELECTRONIC);
        boolean isReal = secKillGoodsItem.secKillGoods.goods.materialType.equals(MaterialType.REAL);

        //电子券必须校验手机号
        if (isElectronic) {
            Validation.required("mobile", mobile);
            Validation.match("mobile", mobile, "^1[3|4|5|8][0-9]\\d{4,8}$");
        }

        //实物券必须校验收货地址信息
        Address defaultAddress = null;
        String receiverMobile = "";
        if (isReal) {
            defaultAddress = Address.findDefault(SecureCAS.getUser());
            if (defaultAddress == null) {
                Validation.addError("address", "validation.required");
            } else {
                receiverMobile = defaultAddress.mobile;
            }
        }

        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            List<String> orderItemsMobiles = OrderItems.getMobiles(user);
            showOrder(secKillGoodsItemId);
            render("SecKillOrders/index.html", user, orderItemsMobiles);
        }

        //创建订单
        Order order = Order.createConsumeOrder(user.getId(), AccountType.CONSUMER);
        if (isElectronic) {
            order.deliveryType = DeliveryType.SMS;
        } else if (isReal) {
            order.deliveryType = DeliveryType.LOGISTICS;
        }
        //记录来源跟踪ID
        if (WebsiteInjector.getUserWebIdentification() != null) {
            order.webIdentificationId = WebsiteInjector.getUserWebIdentification().id;
        }

        if (defaultAddress != null) {
            order.setAddress(defaultAddress);
        }

        //添加订单条目
        try {
            if (isReal) {
                addSecKillOrderItem(order, secKillGoodsItem, count, mobile);
            } else {
                addSecKillOrderItem(order, secKillGoodsItem, count, receiverMobile);
            }

        } catch (NotEnoughInventoryException e) {
            //todo 缺少库存
            Logger.error(e, "inventory not enough");
            error("inventory not enough");
            render("SecKillGoods/index.html");
        }
        order.remark = remark;

        //确认订单.并修改库存
        order.createAndUpdateInventory();
        //扣除秒杀的库存
        secKillGoodsItem.updateInventory(count);


        redirect("/payment_info/" + order.orderNumber);
    }

    private static void addSecKillOrderItem(Order order, SecKillGoodsItem secKillGoodsItem,
                                            long count, String receiverMobile) throws NotEnoughInventoryException {
        System.out.println("count:" + count);
        System.out.println("secKillGoodsItem:" + secKillGoodsItem);
        System.out.println("secKillGoodsItem.baseSale:" + secKillGoodsItem.baseSale);
        if (count > 0 && secKillGoodsItem != null && secKillGoodsItem.baseSale > 0) {
            checkInventory(secKillGoodsItem, count);
            OrderItems orderItem = new OrderItems(order, secKillGoodsItem.secKillGoods.goods, count, receiverMobile,
                    secKillGoodsItem.salePrice, secKillGoodsItem.salePrice);
            orderItem.secKillGoodsItemId = secKillGoodsItem.id;
            order.orderItems.add(orderItem);
            order.amount = order.amount.add(secKillGoodsItem.salePrice.multiply(new BigDecimal(String.valueOf(count))));
            order.needPay = order.amount;
        }
    }

    private static void checkInventory(SecKillGoodsItem secKillGoodsItem,
                                       long count) throws NotEnoughInventoryException {
        if (secKillGoodsItem.baseSale < count) {
            throw new NotEnoughInventoryException();
        }
    }

    /**
     * 计算会员订单明细中已购买的商品
     *
     * @param user    用户ID
     * @param goodsId 商品ID
     * @param number  购买数量
     * @return
     */
    public static boolean checkLimitNumber(User user, Long goodsId, long secKillGoodsItemId,
                                           long number) {

        long boughtNumber = OrderItems.getBoughtNumberOfSecKillGoods(user, goodsId, secKillGoodsItemId);

        //取出商品的限购数量
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        int limitNumber = 0;
        if (goods.limitNumber != null) {
            limitNumber = goods.limitNumber;
        }

        //超过限购数量,则表示已经购买过该商品
        return (limitNumber > 0 && (number > limitNumber || limitNumber <= boughtNumber));
    }

    private static void showOrder(long secKillGoodsItemId) {
        SecKillGoodsItem goodsItem = SecKillGoodsItem.findById(secKillGoodsItemId);
        //解析提交的商品及数量
        //计算电子商品列表和非电子商品列表
        List<Cart> eCartList = new ArrayList<>();
        BigDecimal eCartAmount = BigDecimal.ZERO;
        List<Cart> rCartList = new ArrayList<>();
        BigDecimal rCartAmount = BigDecimal.ZERO;

        System.out.println("secKillGoodsItemId:" + secKillGoodsItemId);

        models.sales.Goods g = goodsItem.secKillGoods.goods;
        g.salePrice = goodsItem.salePrice;
        if (g.materialType == models.sales.MaterialType.REAL) {
            rCartList.add(new Cart(g, 1));
            rCartAmount = goodsItem.salePrice;
        } else if (g.materialType == models.sales.MaterialType.ELECTRONIC) {
            eCartList.add(new Cart(g, 1));
            eCartAmount = goodsItem.salePrice;
        }

        if (rCartList.size() == 0 && eCartList.size() == 0) {
            error("no goods specified");
            return;
        }

        List<Address> addressList = Address.findByOrder(SecureCAS.getUser());
        Address defaultAddress = Address.findDefault(SecureCAS.getUser());

        //如果有实物商品，加上运费
        if (rCartList.size() > 0) {
            rCartAmount = rCartAmount.add(Order.FREIGHT);
        }

        BigDecimal totalAmount = eCartAmount.add(rCartAmount);
        BigDecimal goodsAmount = rCartList.size() == 0 ? eCartAmount : totalAmount.subtract(Order.FREIGHT);

        String items = g.id + "-1";

        renderArgs.put("goodsAmount", goodsAmount);
        renderArgs.put("totalAmount", totalAmount);
        renderArgs.put("addressList", addressList);
        renderArgs.put("address", defaultAddress);
        renderArgs.put("eCartList", eCartList);
        renderArgs.put("eCartAmount", eCartAmount);
        renderArgs.put("rCartList", rCartList);
        renderArgs.put("rCartAmount", rCartAmount);
        renderArgs.put("items", items);
        renderArgs.put("secKillGoodsItemId", secKillGoodsItemId);
    }
}