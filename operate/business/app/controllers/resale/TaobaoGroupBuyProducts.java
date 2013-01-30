package controllers.resale;

import com.taobao.api.ApiException;
import com.taobao.api.request.ItemAddRequest;
import com.taobao.api.response.ItemAddResponse;
import controllers.OperateRbac;
import models.admin.OperateUser;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.sales.Shop;
import models.supplier.Supplier;
import models.wuba.WubaUtil;
import operate.rbac.annotations.ActiveNavigation;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * @author likang
 *         Date: 13-1-29
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class TaobaoGroupBuyProducts extends Controller{
    @ActiveNavigation("resale_partner_product")
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            notFound();
        }
        render(goods);
    }
    @ActiveNavigation("resale_partner_product")
    public static void upload(Long num, Long goodsId, BigDecimal price, String type, String stuffStatus,String title, String desc,
                              String locationState, String locationCity, Long cid) {
        OperateUser operateUser = OperateRbac.currentUser();
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            notFound();
        }

        ResalerProduct product = ResalerProduct.alloc(OuterOrderPartner.TB, goods);

        ItemAddRequest addRequest = new ItemAddRequest();
        addRequest.setNum(num > 999999 ? 999999 : num);// 商品数量
        addRequest.setPrice(price.setScale(2, BigDecimal.ROUND_UP).toString());
        addRequest.setType("fixed");
        addRequest.setStuffStatus("new");
        addRequest.setTitle(goods.title);
        addRequest.setDesc(desc);
        addRequest.setLocationState("上海");
        addRequest.setLocationCity("上海");
        addRequest.setCid(50015759L);//分类类别：餐饮
        //类别：品牌：城市
        addRequest.setProps("2001943:3262426;3816036:3871548;8648185:29423;");
        addRequest.setInputStr(String.valueOf(goods.faceValue.setScale(0))); //面值value
        addRequest.setInputPids("5392163");//面值key
        addRequest.setApproveStatus("instock");//初始为下架的，在淘宝仓库中
        addRequest.setOuterId(String.valueOf(product.id));

        ItemAddResponse response;
        /*
        try {
//            response = taobaoClient.execute(addRequest, token); //执行API请求并打印结果
//            Logger.debug("ItemAddResponse.body:" + response.getBody());
        } catch (ApiException e) {
            Logger.error(e, "add item to taobao failed");
        }
        */

    }
}

