package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.Address;
import models.consumer.User;
import models.order.OrderItems;
import models.sales.PointGoods;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: hejun
 * Date: 12-8-9
 * Time: 下午1:14
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class PointGoodsOrder extends Controller {

    /**
     * 展示订单确认页面
     * @param gid 商品ID
     */
    public static void index(Long gid){
        if (gid == null) {
            error(404, "没有找到该商品！");
        }

        // 查询商品
        PointGoods pointGoods = PointGoods.findById(gid);
//        System.out.println("商品名称 --------- " + pointGoods.name);


        // 解析商品数量
        Map<String, String[]> params = request.params.all();
        String[] numberStr = params.get("g" + gid);
        int number = 1;
        if (numberStr != null && numberStr.length > 0) {
            number = Integer.parseInt(numberStr[0]);
        }
//        System.out.println("购买数量 -------- "+number);

        // 传递商品数量
        renderArgs.put("number",number);

        //获得用户信息
        User user = SecureCAS.getUser();
        //获得用户手机记录
        List<String> orderItems_mobiles = OrderItems.getMobiles(user);

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

        if (rGoodsList.size() == 0 && eGoodsList.size() == 0) {
            error("no goods specified");
            return;
        }

        List<Address> addressList = Address.findByOrder(SecureCAS.getUser());
        Address defaultAddress = Address.findDefault(SecureCAS.getUser());


        // renderArgs.put("goodsAmount", goodsAmount);
        renderArgs.put("totalAmount", totalAmount);
        renderArgs.put("addressList", addressList);
        renderArgs.put("address", defaultAddress);
        renderArgs.put("eGoodsList", eGoodsList);
        renderArgs.put("eGoodsAmount", eGoodsAmount);
        renderArgs.put("rGoodsList", rGoodsList);
        renderArgs.put("rGoodsAmount", rGoodsAmount);
        render(user, orderItems_mobiles);

    }

//    /**
//     * 生成订单并存至数据库
//     * @param gid       积分商品ID
//     * @param number    积分商品购买数量
//     * @param mobile    接收手机
//     * @param remark    附言
//     */
//    public static void create(Long gid, int number, String mobile, String remark){
//
//        //如果订单中有电子券，则必须填写手机号
//        Http.Cookie cookie = request.cookies.get("identity");
//        String cookieValue = cookie == null ? null : cookie.value;
//        User user = SecureCAS.getUser();
//
//        //查找商品
//        PointGoods pointGoods = PointGoods.findById(gid);
//
//        //电子券必须校验手机号
//        if (pointGoods.materialType == MaterialType.ELECTRONIC) {
//            Validation.required("mobile", mobile);
//            Validation.match("mobile", mobile, "^1\\d{10}$");
//        }
//
//        //实物券必须校验收货地址信息
//        Address defaultAddress = null;
//        String receiverMobile = "";
//        if (pointGoods.materialType == MaterialType.ELECTRONIC) {
//            defaultAddress = Address.findDefault(SecureCAS.getUser());
//            if (defaultAddress == null) {
//                Validation.addError("address", "validation.required");
//            } else {
//                receiverMobile = defaultAddress.mobile;
//            }
//        }
//
//        //审查订单信息，如有错误，则跳转提示
//        if (Validation.hasErrors()) {
//            for (String key : validation.errorsMap().keySet()) {
//                warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
//            }
//
//            //计算电子商品列表和非电子商品列表
//            List<PointGoods> eGoodsList = new ArrayList<>();
//            int eGoodsAmount = 0;
//            List<PointGoods> rGoodsList = new ArrayList<>();
//            int rGoodsAmount = 0;
//
//            if (pointGoods.materialType == models.sales.MaterialType.REAL){
//                rGoodsList.add(pointGoods);
//                rGoodsAmount = pointGoods.pointPrice.intValue() * number;
//            }
//            else{
//                eGoodsList.add(pointGoods);
//                eGoodsAmount = pointGoods.pointPrice.intValue() * number;
//            }
//            int totalAmount = eGoodsAmount + rGoodsAmount;
//
//            List<Address> addressList = Address.findByOrder(SecureCAS.getUser());
//
//            renderArgs.put("totalAmount", totalAmount);
//            renderArgs.put("addressList", addressList);
//            renderArgs.put("address", defaultAddress);
//            renderArgs.put("eGoodsList", eGoodsList);
//            renderArgs.put("eGoodsAmount", eGoodsAmount);
//            renderArgs.put("rGoodsList", rGoodsList);
//            renderArgs.put("rGoodsAmount", rGoodsAmount);
//            List<String> orderItems_mobiles = OrderItems.getMobiles(user);
//            render("PointGoodsOrder/index.html", user, orderItems_mobiles);
//        }
//        // 创建订单
//        try {
//            models.order.PointGoodsOrder pointGoodsOrder = new models.order.PointGoodsOrder(user.id,pointGoods,new Long(number));
//            // 判断是否需要物流
//            if (pointGoodsOrder.containsRealGoods()){
//                pointGoodsOrder.deliveryType = DeliveryType.LOGISTICS;
//                if (defaultAddress != null){
//                    pointGoodsOrder.setAddress(defaultAddress);
//                }
//            }
//            else{
//                pointGoodsOrder.deliveryType = DeliveryType.SMS;
//                pointGoodsOrder.buyerMobile = mobile;
//            }
//            // 添加用户的留言信息
//            pointGoodsOrder.remark = remark;
//            System.out.println("order ");
//            // 跳转兑换明细
//            redirect("/payment_info/" + pointGoodsOrder.orderNumber);
//        }
//        catch (NotEnoughInventoryException e){
//            Logger.error(e, "inventory not enough");
//            error(404, "商品库存不足，很抱歉给您造成不便！");
//        }
//
//
//    }

}
