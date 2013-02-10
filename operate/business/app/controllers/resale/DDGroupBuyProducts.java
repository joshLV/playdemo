package controllers.resale;

import com.google.gson.Gson;
import controllers.OperateRbac;
import models.operator.OperateUser;
import models.dangdang.groupbuy.DDGroupBuyUtil;
import models.dangdang.groupbuy.DDResponse;
import models.order.OuterOrderPartner;
import models.sales.*;
import operate.rbac.annotations.ActiveNavigation;
import org.w3c.dom.Node;
import play.libs.XPath;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-1-16
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class DDGroupBuyProducts extends Controller {
    public static final String PRODUCT_URL = "http://tuan.dangdang.com/product.php?product_id=";

    @ActiveNavigation("resale_partner_product")
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            notFound();
        }
        render(goods);
    }

    @ActiveNavigation("resale_partner_product")
    public static void upload(Long goodsId ) {
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            notFound();
        }
        OperateUser operateUser = OperateRbac.currentUser();
        //准备参数
        Map<String, String> groupbuyInfoParams = params.allSimple();
        groupbuyInfoParams.remove("body");
        groupbuyInfoParams.remove("goodsId");
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.putAll(groupbuyInfoParams);

        ResalerProduct product = ResalerProduct.alloc(OuterOrderPartner.DD, goods);
        templateParams.put("linkId", String.valueOf(product.goodsLinkId));
        product.latestJson(new Gson().toJson(templateParams)).save();//添加shop前先把参数给输出了
        Collection<Shop> shops = goods.getShopList();
        templateParams.put("shops", shops);

        DDResponse response = DDGroupBuyUtil.pushGoods(templateParams);
        if (response.isOk()) {
            product.status(ResalerProductStatus.UPLOADED).creator(operateUser.id).save();
            //记录历史
            ResalerProductJournal.createJournal(product, operateUser.id, product.latestJsonData,
                    ResalerProductJournalType.CREATE, "上传商品");
            //查询当当的商品ID
            Node node = DDGroupBuyUtil.getJustUploadedTeam(product.goodsLinkId);
            if (node != null) {
                product.partnerProductId = Long.parseLong(XPath.selectText("//ddgid", node).trim());
                product.url = PRODUCT_URL + product.partnerProductId;
                product.save();
            }
        }else {
            product.delete();
        }
        render("resale/DDGroupBuyProducts/result.html", response);
    }
}

