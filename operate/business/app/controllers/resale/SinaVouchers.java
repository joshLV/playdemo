package controllers.resale;

import com.google.gson.Gson;
import controllers.OperateRbac;
import models.sales.Goods;
import models.sales.Shop;
import models.sina.SinaVoucherResponse;
import models.sina.SinaVoucherUtil;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;

/**
 * User: yan
 * Date: 13-3-21
 * Time: 上午10:26
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class SinaVouchers extends Controller {
    @ActiveNavigation("resale_partner_product")
    public static void showUpload(long goodsId) {
        Goods goods = Goods.findById(goodsId);

        Collection<Shop> shops = goods.getShopList();
        Supplier supplier = Supplier.findById(goods.supplierId);

        render(goods, supplier, shops);
    }

    @ActiveNavigation("resale_partner_product")
    public static void upload() {

        Map<String, String> allParams = params.allSimple();
        //准备卡券描述
        Map<String, String> description = new HashMap<>();
        description.put("description", allParams.remove("description"));
        description.put("readme", allParams.remove("readme"));
        allParams.remove("body");

        //准备请求参数
        Map<String, Object> requestParams = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            requestParams.put(entry.getKey(), entry.getValue());
        }
        List<Map<String, String>> descriptions = new ArrayList<>();
        descriptions.add(description);

        requestParams.put("descriptions", descriptions);

        SinaVoucherResponse response = SinaVoucherUtil.uploadTemplate(new Gson().toJson(requestParams));
        render("resale/SinaVouchers/result.html", response);
    }
}
