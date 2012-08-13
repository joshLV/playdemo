package controllers;

import controllers.modules.website.cas.SecureCAS;
import models.consumer.Address;
import models.consumer.User;
import models.order.Cart;
import models.order.OrderItems;
import play.mvc.Controller;
import models.sales.PointGoods;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-9
 * Time: 下午1:14
 * To change this template use File | Settings | File Templates.
 */
@With({SecureCAS.class, WebsiteInjector.class})
public class PointGoodsOrder extends Controller {

    public static void index(Long gid){

        // 查询商品
        PointGoods pointGoods = PointGoods.findById(gid);
        System.out.println("商品名称 --------- " + pointGoods.name);
        if (pointGoods == null) {
            error(404, "没有找到该商品！");
        }

        // 解析商品数量
        Map<String, String[]> params = request.params.all();
        System.out.println("参数长度 ++++++ "+params.size());
        String[] numberStr = params.get("g" + gid);
        int number = 1;
        if (numberStr != null && numberStr.length > 0) {
            number = Integer.parseInt(numberStr[0]);
        }
        System.out.println("购买数量 -------- "+number);
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

    public static void create(Long rGoodsId, Long eGoodsId, String mobile, String remark){
        System.out.println("R  ID   "+ rGoodsId);
        System.out.println("E  ID   "+ eGoodsId);

    }

}
