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
import models.sales.SecKillGoodsStatus;
import play.Logger;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
    public static void index() {

        Long secKillGoodsItemId = Long.parseLong(request.params.get("secKillGoodsItemId") == null ? "0" : request.params.get("secKillGoodsItemId"));

//        SecKillGoodsItem secKillGoodsItem = SecKillGoodsItem.findById(secKillGoodsItemId);
        SecKillGoodsItem secKillGoodsItem = SecKillGoodsItem.find("id=? and secKillEndAt>=? and secKillBeginAt<=? and status=?", secKillGoodsItemId, new Date(), new Date(), SecKillGoodsStatus.ONSALE).first();
        if (secKillGoodsItem == null) {
            error("no secKillGoods specified");
            return;
        }
        //检查库存
        try {

            checkInventory(secKillGoodsItem, 1);
        } catch (NotEnoughInventoryException e) {
            //缺少库存
            Logger.info(e, "Inventory not enough,goodsId:" + secKillGoodsItem.secKillGoods.goods.id);
            redirect("/seckill-goods");
        }

        User user = SecureCAS.getUser();
        List<String> orderItemsMobiles = OrderItems.getMobiles(user);
        showOrder(user, secKillGoodsItemId);

        render(user, orderItemsMobiles);
    }

    /**
     * 创建秒杀商品的订单.
     */
    public static void create(long secKillGoodsItemId, long secKillGoodsId, String mobile, String remark) {
        long count = 1;
        //如果订单中有电子券，则必须填写手机号
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;
        User user = SecureCAS.getUser();

        //解析提交的商品及数量
//        SecKillGoodsItem secKillGoodsItem = SecKillGoodsItem.findById(secKillGoodsItemId);
        SecKillGoodsItem secKillGoodsItem = SecKillGoodsItem.find("id=? and secKillEndAt>=? and secKillBeginAt<=? and status=?", secKillGoodsItemId, new Date(), new Date(), SecKillGoodsStatus.ONSALE).first();
        if (secKillGoodsItem == null) {
            error("no secKillGoods specified");
            return;
        }

        //判断帐号限购
        boolean exceedLimit = OrderItems.checkLimitNumber(user, secKillGoodsItem.secKillGoods.goods.id, secKillGoodsId, count);

        if (exceedLimit) {
            //todo 页面实现限购提示

            redirect("/seckill-goods?exceedLimit=" + exceedLimit);
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

            defaultAddress = Address.findDefault(user);
            if (defaultAddress == null) {
                Validation.addError("address", "validation.required");
            } else {
                receiverMobile = defaultAddress.mobile;
            }
        }


        /*for (Error error : Validation.errors()) {

            System.out.println(error.message());
        }*/


        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            List<String> orderItemsMobiles = OrderItems.getMobiles(user);
            showOrder(user, secKillGoodsItemId);
            render("SecKillOrders/index.html", user, orderItemsMobiles);
        }

        Order order = null;

        try {
            order = doCreateSecKillOrder(mobile, remark, count, user,
                    secKillGoodsItem, isElectronic, isReal, defaultAddress,
                    receiverMobile);

        } catch (NotEnoughInventoryException e) {
            // 缺少库存
            Logger.info(e, "Inventory not enough,goodsId:"
                    + secKillGoodsItem.secKillGoods.goods.id);
            redirect("/seckill-goods");
        } catch (Exception e) {
            Logger.error(e, "出现异常，事务回滚");
            redirect("/seckill-goods");
        }

        if (order == null) {
            Logger.error("没有建立订单，这是不可能出现的，请检查代码");
            redirect("/seckill-goods");
        }

        redirect("/payment_info/" + order.orderNumber);
    }

    private static Order doCreateSecKillOrder(String mobile, String remark,
                                              long count, User user, SecKillGoodsItem secKillGoodsItem,
                                              boolean isElectronic, boolean isReal, Address defaultAddress,
                                              String receiverMobile) throws NotEnoughInventoryException {
        //创建订单
        Order order = Order.createYbqConsumeOrder(user.getId(), AccountType.CONSUMER);
        if (isElectronic) {
            order.deliveryType = DeliveryType.SMS;
        } else if (isReal) {
            order.deliveryType = DeliveryType.LOGISTICS;
        }
        //记录来源跟踪ID
        if (WebsiteInjector.getUserWebIdentification() != null) {
            order.webIdentificationId = WebsiteInjector.getUserWebIdentification().getSavedId();
        }

        if (defaultAddress != null) {
            order.setAddress(defaultAddress);
        }

        //添加订单条目
        if (isElectronic) {
            addSecKillOrderItem(order, secKillGoodsItem, count, mobile);
        } else {
            addSecKillOrderItem(order, secKillGoodsItem, count, receiverMobile);
        }
        order.remark = remark;

//        //确认订单.并修改库存
        order.createAndUpdateInventory();
        //扣除秒杀的库存
        secKillGoodsItem.updateInventory(count);
        return order;
    }


    private static void addSecKillOrderItem(Order order, SecKillGoodsItem secKillGoodsItem,
                                            long count, String receiverMobile) throws NotEnoughInventoryException {
        if (count <= 0) {
            throw new IllegalArgumentException("count:" + count);
        }

        checkInventory(secKillGoodsItem, count);
        OrderItems orderItem = new OrderItems(order, secKillGoodsItem.secKillGoods.goods, count, receiverMobile,
                secKillGoodsItem.secKillGoods.goods.salePrice, secKillGoodsItem.secKillGoods.goods.salePrice);
        orderItem.secKillGoods = secKillGoodsItem.secKillGoods;
        orderItem.secKillGoodsItemId = secKillGoodsItem.id;
        orderItem.rebateValue = secKillGoodsItem.secKillGoods.goods.salePrice.subtract(secKillGoodsItem.salePrice).multiply(new BigDecimal(count));
        order.rebateValue = BigDecimal.ZERO;
        order.orderItems.add(orderItem);
        order.amount = order.amount.add(secKillGoodsItem.salePrice.multiply(new BigDecimal(count)));
        order.needPay = order.amount.subtract(order.rebateValue);
    }

    private static void checkInventory(SecKillGoodsItem secKillGoodsItem,
                                       long count) throws NotEnoughInventoryException {

        if (secKillGoodsItem.baseSale <= 0 || secKillGoodsItem.baseSale < count) {
            throw new NotEnoughInventoryException();
        }
    }

    private static void showOrder(User user, long secKillGoodsItemId) {
        SecKillGoodsItem goodsItem = SecKillGoodsItem.findById(secKillGoodsItemId);
        if (goodsItem == null) {
            error("no secKillGoods specified");
            return;
        }
        //判断帐号限购

        boolean exceedLimit = OrderItems.checkLimitNumber(user, goodsItem.secKillGoods.goods.id, goodsItem.secKillGoods.id, 1);

        if (exceedLimit) {
            renderArgs.put("exceedLimit", exceedLimit);
        }
        //解析提交的商品及数量
        //计算电子商品列表和非电子商品列表
        List<Cart> eCartList = new ArrayList<>();
        BigDecimal eCartAmount = BigDecimal.ZERO;
        List<Cart> rCartList = new ArrayList<>();
        BigDecimal rCartAmount = BigDecimal.ZERO;

        models.sales.Goods g = goodsItem.secKillGoods.goods;

        g.salePrice = goodsItem.salePrice;


        if (g.materialType == models.sales.MaterialType.REAL) {

            rCartList.add(new Cart(g, 1, goodsItem));
            rCartAmount = goodsItem.salePrice;
        } else if (g.materialType == models.sales.MaterialType.ELECTRONIC) {

            eCartList.add(new Cart(g, 1, goodsItem));
            eCartAmount = goodsItem.salePrice;
        }

        if (rCartList.size() == 0 && eCartList.size() == 0) {
            error("没有找到指定商品！");
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
        renderArgs.put("secKillGoodsId", goodsItem.secKillGoods.id);
    }
}