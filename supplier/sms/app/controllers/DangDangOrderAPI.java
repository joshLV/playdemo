package controllers;

import models.dangdang.DDOrder;
import models.dangdang.DangDangApiUtil;
import models.accounts.AccountType;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.resale.ResalerLevel;
import models.sales.Goods;
import models.resale.Resaler;
import models.resale.ResalerStatus;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;

import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-13
 * Time: 下午3:59
 */
public class DangDangOrderAPI extends Controller {
    public static final String app_key = "";


    public static void createOrder(String sign) {

        //取得参数信息 必填信息
        Map<String, String> params = DangDangApiUtil.filterPlayParameter(request.params.all());
        String id = params.get("id");
        String all_amount = params.get("all_amount");
        String amount = params.get("amount");
        String user_mobile = params.get("user_mobile");
        String options = params.get("options");
        String express_memo = params.get("express_memo");

        String user_id = params.get("user_id");
        //检查参数
        if (isBlank(params.get("user_mobile")) || isBlank(user_id)) {
            Logger.error("invalid userInfo: %s", user_id);
            render(""); //todo
        }
        String kx_order_id = params.get("kx_order_id");
        if (isBlank(kx_order_id)) {
            Logger.error("invalid kx_order_id: %s", kx_order_id);
            render(); //todo
        }
        if (isBlank(sign)) {
            Logger.error("invalid sign: %s", sign);
            render(); //todo
        }

        //校验参数
        SortedMap<String, String> veryParams = new TreeMap<>();
        veryParams.put("kx_order_id", kx_order_id);
        if (!DangDangApiUtil.validSign(veryParams, "", "", sign)) {
            Logger.error("wrong sign: ", sign);
            render("");
        }
        Order order = null;
        //如果已经存在订单，则不处理，直接返回xml
        DDOrder ddOrder = DDOrder.find("orderId=?", kx_order_id).first();
        if (ddOrder != null) {
            order = Order.find("ddOrder=?", ddOrder).first();
            if (order != null) {
                render(order, id, kx_order_id);
            }
        }


        //定位请求者
        Resaler resaler = Resaler.find("byKey", app_key).first();
        if (resaler == null || resaler.status != ResalerStatus.APPROVED) {
            Logger.error("unavailable app_key: ", app_key);
            render("");//todo
        }
        //产生DD订单
        ddOrder = new DDOrder(Long.parseLong(kx_order_id), new BigDecimal(all_amount), new BigDecimal(amount), resaler.id).save();

        try {
            JPA.em().flush();
        } catch (Exception e) {
            render();
        }

        JPA.em().refresh(ddOrder, LockModeType.PESSIMISTIC_WRITE);

        order = Order.createConsumeOrder(resaler.getId(), AccountType.RESALER);

        //分解有几个商品，每个商品购买的数量
        String[] arrGoods = options.split(",");
        String[] arrGoodsItem = null;
        for (String goodsItem : arrGoods) {
            arrGoodsItem = goodsItem.split(":");
            if (arrGoodsItem != null) {
                Goods goods = Goods.findById(Long.parseLong(arrGoodsItem[0]));
                BigDecimal resalerPrice = goods.getResalePrice(ResalerLevel.NORMAL);
                try {
                    ddOrder.addOrderItem(goods, Integer.parseInt(arrGoodsItem[1]), user_mobile, resalerPrice);
                    order.addOrderItem(goods, Integer.parseInt(arrGoodsItem[1]), user_mobile, resalerPrice, resalerPrice);
                } catch (NotEnoughInventoryException e) {
                    Logger.info("inventory not enough");
                }
            }
        }

        order.remark = express_memo;
        order.createAndUpdateInventory();
        order.payAndSendECoupon();
        render(order, id, kx_order_id);

    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().equals("");
    }
}
