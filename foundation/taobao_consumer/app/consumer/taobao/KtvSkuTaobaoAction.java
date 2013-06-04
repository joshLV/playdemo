package consumer.taobao;

import models.ktv.KtvProductGoods;
import models.ktv.KtvSkuTaobaoMessage;
import models.ktv.KtvTaobaoSku;
import models.ktv.KtvTaobaoUtil;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.taobao.KtvSkuMessageUtil;
import play.Logger;

public class KtvSkuTaobaoAction {

    public void processMessage(Long ktvProductGoodsId) {
        try {
            Thread.sleep(2000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Logger.info("message.ktvProductGoodsId:%s,message.productGoodsId:%s", ktvProductGoodsId);
        //根据价格策略更新sku
        if (ktvProductGoodsId != null) {
            KtvProductGoods productGoods = KtvProductGoods.findById(ktvProductGoodsId);
            if (productGoods != null) {
                KtvTaobaoUtil.updateTaobaoSkuByProductGoods(productGoods);
            }else {
                Logger.info("KtvSkuConsumer process failed: KtvProductGoods is null");
            }
        } else {
            Logger.info("KtvSkuConsumer process failed: ktvProductGoodsId is null");
        }
    }
}
