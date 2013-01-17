package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.Address;
import models.consumer.User;
import models.consumer.UserInfo;
import models.consumer.UserPoint;
import models.order.DeliveryType;
import models.order.NotEnoughInventoryException;
import models.order.OrderItems;
import models.order.PointGoodsOrder;
import models.sales.MaterialType;
import models.sales.PointGoods;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

import static play.Logger.warn;

/**
 * User: hejun
 * Date: 12-8-14
 * Time: 上午10:47
 */

@With({SecureCAS.class, WebsiteInjector.class})
public class PointGoodsPaymentInfo extends Controller {

    public static void index(Long gid, int number, String mobile, String remark){

        //加载用户账户信息
        User user = SecureCAS.getUser();
        PointGoods pointGoods = PointGoods.findById(gid);
        long amount = pointGoods.pointPrice * number;
        UserInfo userInfo = UserInfo.findByUser(user);

        renderArgs.put("pointGoods",pointGoods);
        renderArgs.put("gid",gid);
        renderArgs.put("number", number);
        renderArgs.put("amount", amount);
        renderArgs.put("userInfo",userInfo);
        renderArgs.put("mobile",mobile);
        renderArgs.put("remark",remark);
        render(user);
    }

    /**
     * 生成订单并存至数据库
     * @param goodsId   积分商品ID
     * @param number    积分商品购买数量
     * @param mobile    接收手机
     * @param remark    附言
     */
    public static void create(Long goodsId, int number, String mobile, String remark){

        //如果订单中有电子券，则必须填写手机号
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;
        User user = SecureCAS.getUser();

        //查找商品
        PointGoods pointGoods = PointGoods.findById(goodsId);

        //电子券必须校验手机号
        if (pointGoods.materialType == MaterialType.ELECTRONIC) {
            Validation.required("mobile", mobile);
            Validation.match("mobile", mobile, "^1\\d{10}$");
        }

        //实物券必须校验收货地址信息
        Address defaultAddress = null;
        String receiverMobile = mobile;
        if (pointGoods.materialType == MaterialType.REAL) {
            defaultAddress = Address.findDefault(SecureCAS.getUser());

            if (defaultAddress == null) {
                Validation.addError("address", "validation.required");
            } else {
                receiverMobile = defaultAddress.mobile;
            }
        }

        //审查订单信息，如有错误，则跳转提示
        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }

            //计算电子商品列表和非电子商品列表
            List<PointGoods> eGoodsList = new ArrayList<>();
            int eGoodsAmount = 0;
            List<PointGoods> rGoodsList = new ArrayList<>();
            int rGoodsAmount = 0;

            if (pointGoods.materialType == models.sales.MaterialType.REAL){
                rGoodsList.add(pointGoods);
                rGoodsAmount = pointGoods.pointPrice.intValue() * number;
            }
            else{
                eGoodsList.add(pointGoods);
                eGoodsAmount = pointGoods.pointPrice.intValue() * number;
            }
            int totalAmount = eGoodsAmount + rGoodsAmount;

            List<Address> addressList = Address.findByOrder(SecureCAS.getUser());

            renderArgs.put("totalAmount", totalAmount);
            renderArgs.put("addressList", addressList);
            renderArgs.put("address", defaultAddress);
            renderArgs.put("eGoodsList", eGoodsList);
            renderArgs.put("eGoodsAmount", eGoodsAmount);
            renderArgs.put("rGoodsList", rGoodsList);
            renderArgs.put("rGoodsAmount", rGoodsAmount);
            List<String> orderItems_mobiles = OrderItems.getMobiles(user);
            render("PointGoodsOrder/index.html", user, orderItems_mobiles);
        }

        // 创建订单
        try {
            models.order.PointGoodsOrder pointGoodsOrder = new models.order.PointGoodsOrder(user.id,pointGoods,new Long(number));
            // 判断是否需要物流
            if (pointGoodsOrder.containsRealGoods()){
                pointGoodsOrder.deliveryType = DeliveryType.LOGISTICS;
                if (defaultAddress != null){
                    pointGoodsOrder.setAddress(defaultAddress);
                }
            }
            else{
                pointGoodsOrder.deliveryType = DeliveryType.SMS;
                pointGoodsOrder.receiverMobile = receiverMobile;
            }
            // 添加用户的留言信息
            pointGoodsOrder.remark = remark;
            // 创建订单，减少库存，增加销量，扣除积分
            pointGoodsOrder.createAndUpdateInventory();
//            System.out.println("执行 创建订单");

            // 添加用户积分使用记录
            UserPoint userPoint = new UserPoint();
            userPoint.addRecord(user,"127","0",pointGoodsOrder.amount,pointGoodsOrder.totalPoint);
//            System.out.println("添加 用户积分使用记录");

            // 跳转兑换明细
            redirect("/payment_info/" + pointGoodsOrder.orderNumber);
        }
        catch (NotEnoughInventoryException e){
            error(404, "商品库存不足，很抱歉给您造成不便！");
        }

    }

    public static void success(String orderNumber){
        PointGoodsOrder order = PointGoodsOrder.findByOrderNumber(orderNumber);
        if (order == null){
            error(404, "没有找到该商品！");
        }
        render();
    }
}
