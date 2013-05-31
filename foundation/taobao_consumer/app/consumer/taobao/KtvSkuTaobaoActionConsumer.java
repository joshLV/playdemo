package consumer.taobao;

import models.RabbitMQConsumerWithTx;
import models.ktv.KtvSkuTaobaoMessage;
import models.ktv.KtvTaobaoSku;
import models.ktv.KtvTaobaoUtil;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.taobao.KtvSkuMessageUtil;
import play.Logger;
import play.jobs.OnApplicationStart;

/**
 * @author likang
 *         Date: 13-5-31
 */

@OnApplicationStart(async = true)
public class KtvSkuTaobaoActionConsumer extends RabbitMQConsumerWithTx<KtvSkuTaobaoMessage>{
    @Override
    public void consumeWithTx(KtvSkuTaobaoMessage message) {
        ResalerProduct resalerProduct = ResalerProduct.findById(message.resalerProductId);
        if (resalerProduct == null) {
            Logger.error("taobao add sku error: reslaer product not found %s", message.resalerProductId);
            return;
        }

        if (message.action == KtvSkuMessageUtil.ACTION_ADD) {
            KtvTaobaoSku sku = new KtvTaobaoSku();
            sku.goods = Goods.findById(message.goodsId);
            sku.setRoomType(message.roomType);
            sku.setDate(message.date);
            sku.setDay(message.day);
            sku.setTimeRange(message.timeRange);
            sku.setTimeRangeCode(message.timeRangeCode);
            sku.price = message.price;
            sku.quantity = message.quantity;
            sku.createdAt = message.createdAt;

            if (KtvTaobaoUtil.addSaleSkuOnTaobao(sku, resalerProduct)) {
                sku.save();
            }
        }else if (message.action == KtvSkuMessageUtil.ACTION_UPDATE) {
            KtvTaobaoSku sku = KtvTaobaoSku.findById(message.skuId);
            if (sku == null) {
                Logger.error("taobao update sku error: sku not found %s", message.skuId);
                return;
            }

            sku.price = message.price;
            sku.quantity = message.quantity;

            if (KtvTaobaoUtil.updateSaleSkuOnTaobao(sku, resalerProduct)) {
                sku.save();
            }
        }else if (message.action == KtvSkuMessageUtil.ACTION_DELETE) {
            KtvTaobaoSku sku = KtvTaobaoSku.findById(message.skuId);
            if (sku == null) {
                Logger.error("taobao update sku error: sku not found %s", message.skuId);
                return;
            }
            if (KtvTaobaoUtil.deleteSaleSkuOnTaobao(sku, resalerProduct)) {
                sku.delete();
            }
        }
    }

    @Override
    protected Class getMessageType() {
        return KtvSkuTaobaoMessage.class;
    }

    @Override
    protected String queue() {
        return KtvSkuMessageUtil.TAOBAO_SKU_QUEUE_NAME;
    }
}
