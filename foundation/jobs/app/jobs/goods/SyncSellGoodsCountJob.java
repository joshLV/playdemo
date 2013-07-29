package jobs.goods;

import models.jingdong.groupbuy.JDGroupBuyUtil;
import models.jingdong.groupbuy.JingdongMessage;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.order.Order;
import models.order.OuterOrderPartner;
import models.sales.ResalerProduct;
import models.supplier.Supplier;
import models.wuba.WubaResponse;
import models.wuba.WubaUtil;
import models.yihaodian.YHDResponse;
import models.yihaodian.YHDUtil;
import play.Logger;
import play.jobs.Every;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wangjia
 * Date: 13-7-26
 * Time: 下午3:23
 */
@JobDefine(title = "58，一号店,京东导入券商品销量同步", description = "每三小时更新同步58,一号店，京东的商品售出数量")
@Every("3h")
//@OnApplicationStart
public class SyncSellGoodsCountJob extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        List<OuterOrderPartner> partners = new ArrayList<>();
        partners.add(OuterOrderPartner.JD);
        partners.add(OuterOrderPartner.WB);
        partners.add(OuterOrderPartner.YHD);

        //同步京东,58,一号店库存
        List<ResalerProduct> products = ResalerProduct.getResalerImportedProducts(partners);
        for (ResalerProduct product : products) {
            //商品库存不足15件，发送提醒邮件
            Long baseCount = product.goods.getRealStocks();
            if (product.goods.getRealStocks() < 15l) {
                Map<String, Object> paramsMap = new HashMap<>();
                Supplier supplier = Supplier.findById(product.goods.supplierId);
                paramsMap.put("supplierName", supplier.fullName);
                paramsMap.put("goodsName", product.goods.name);
                paramsMap.put("faceValue", product.goods.faceValue);
                paramsMap.put("supplierName", baseCount);
                paramsMap.put("offSalesFlag", "noInventory");
                Order.sendInventoryNotEnoughMail(paramsMap);
            }
            //同步京东
            if (product.partner == OuterOrderPartner.JD) {
                Map<String, Object> params = new HashMap<>();
                params.put("VenderTeamId", product.goodsLinkId);
                params.put("JdTeamId", product.partnerProductId);
                params.put("MaxNumber", baseCount);
                //提交请求
                JingdongMessage response = JDGroupBuyUtil.sendRequest("updateMaxNumber", params);
                if (!response.isOk()) {
                    Logger.error("同步京东库存失败 %s:%s 分销商品Id: %s", response.resultCode, response.resultMessage,
                            product.id.toString());
                }
                Logger.info("同步京东库存成功 分销商品Id: %s", product.id.toString());
            }
            //同步58库存
            else if (product.partner == OuterOrderPartner.WB) {
                Map<String, Object> wubaParams = new HashMap<>();
                wubaParams.put("groupbuyId", product.goodsLinkId);
                wubaParams.put("num", product.goods.getRealStocks());

                //发起请求
                WubaResponse response = WubaUtil.sendRequest(wubaParams, "emc.groupbuy.changeinventory", false);
                if (!response.isOk()) {
                    Logger.error("同步58库存失败 %s:%s 分销商品Id: %s", response.code, response.msg,
                            product.id.toString());
                }
                Logger.info("同步58库存成功 分销商品Id: %s", product.id.toString());
            }
            //同步一号店库存
            else if (product.partner == OuterOrderPartner.YHD) {
                Map<String, String> requestParams = new HashMap<>();
                requestParams.put("productId", String.valueOf(product.partnerProductId));
                requestParams.put("virtualStockNum", String.valueOf(product.goods.getRealStocks()));
                YHDResponse response = YHDUtil.sendRequest(requestParams, "yhd.product.stock.update", "updateCount");
                if (!response.isOk()) {
                    Logger.error("同步一号店库存失败 %s:%s 分销商品Id: %s", response.firstErrorCode(), response.firstErrorDes(),
                            product.id.toString());
                }
                Logger.info("同步一号店库存成功 分销商品Id: %s", product.id.toString());
            }
        }

    }

}
