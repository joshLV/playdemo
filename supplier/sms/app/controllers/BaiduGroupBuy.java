package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.baidu.BaiduUtil;
import models.order.OuterOrderPartner;
import models.sales.GoodsStatus;
import models.sales.ResalerProduct;
import play.mvc.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wangjia
 * Date: 13-7-26
 * Time: 上午11:18
 */
public class BaiduGroupBuy extends Controller {
    /**
     * 百度拉取第三方团购销量
     *
     * @return
     */
    public static void getSellCount(String bd_se_key) {
        Map<String, Object> result = new HashMap<>();
        System.out.println(" BaiduUtil.sign() = " + BaiduUtil.sign());
        if (!BaiduUtil.sign().equals(bd_se_key)) {
            putErrnonoAndErrmsg(result, "-2", "bd_se_key校验失败");
            renderJSON(result);
        }

        //取得baidu分销商商品库中在售的商品（不包含抽奖商品）
        List<ResalerProduct> products = ResalerProduct.find("partner=? and " +
                "goods.deleted=? and goods.status=? and goods.expireAt >=? and goods.isLottery = false order by createdAt DESC",
                OuterOrderPartner.BD, DeletedStatus.UN_DELETED, GoodsStatus.ONSALE,
                new Date()).fetch();
        List<Map<String, Object>> paramList = new ArrayList<>();
        for (ResalerProduct product : products) {
            Map<String, Object> param = new HashMap<>();
            param.put(product.partnerProductId, product.goods.getRealSaleCount());
            paramList.add(param);
        }
        putErrnonoAndErrmsg(result, "0", "");
        result.put("data", paramList);
        renderJSON(result);
    }

    private static void putErrnonoAndErrmsg(Map<String, Object> result, String errno, String errmsg) {
        result.put("errno", errno);
        result.put("errmsg", errmsg);
    }
}
