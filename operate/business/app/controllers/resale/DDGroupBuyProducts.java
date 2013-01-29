package controllers.resale;

import com.google.gson.Gson;
import controllers.OperateRbac;
import models.admin.OperateUser;
import models.dangdang.groupbuy.DDGroupBuyUtil;
import models.dangdang.groupbuy.DDResponse;
import models.order.OuterOrderPartner;
import models.resale.ResalerProduct;
import models.resale.ResalerProductJournal;
import models.resale.ResalerProductJournalType;
import models.sales.Goods;
import models.sales.GoodsDeployRelation;
import models.sales.Shop;
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
        //准备参数
        Map<String, String> groupbuyInfoParams = params.allSimple();
        groupbuyInfoParams.remove("body");
        groupbuyInfoParams.remove("goodsId");
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.putAll(groupbuyInfoParams);

        GoodsDeployRelation relation = GoodsDeployRelation.generate(goods, OuterOrderPartner.DD);
        templateParams.put("linkId", String.valueOf(relation.linkId));
        String jsonData = new Gson().toJson(templateParams);//添加shop前先把参数给输出了
        Collection<Shop> shops = goods.getShopList();
        templateParams.put("shops", shops);

        OperateUser operateUser = OperateRbac.currentUser();

        DDResponse response = DDGroupBuyUtil.pushGoods(templateParams);
        if (response.isOk()) {
            //记录历史
            ResalerProduct product = ResalerProduct.createProduct(OuterOrderPartner.DD, 0L, operateUser.id, goods, relation.linkId);
            ResalerProductJournal.createJournal(product, operateUser.id, jsonData, ResalerProductJournalType.CREATE, "上传商品");
            //查询当当的商品ID
            Node node = DDGroupBuyUtil.getJustUploadedTeam(relation.linkId);
            if (node != null) {
                product.partnerProductId = Long.parseLong(XPath.selectText("//ddgid", node));
                product.url = PRODUCT_URL + product.partnerProductId;
                product.save();
            }
        }
        render("resale/DDGroupBuyProducts/result.html", response);
    }
}

