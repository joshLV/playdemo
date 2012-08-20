package factory.sales;

import factory.ModelFactory;
import factory.annotation.Factory;
import models.sales.SecKillGoodsItem;
import util.DateHelper;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-16
 * Time: 上午10:41
 */
public class SecKillGoodsItemFactory extends ModelFactory<SecKillGoodsItem> {

    @Override
    public SecKillGoodsItem define() {
        SecKillGoodsItem secKillGoodsItem = new SecKillGoodsItem();
        secKillGoodsItem.virtualInventory = 1l;
        secKillGoodsItem.goodsTitle = "第一波秒杀";
        secKillGoodsItem.saleCount = 0;
        secKillGoodsItem.salePrice = new BigDecimal(10);
        secKillGoodsItem.secKillBeginAt = new Date();
        secKillGoodsItem.secKillEndAt = DateHelper.afterMinuts(new Date(), 10);
        return secKillGoodsItem;
    }

    @Factory(name = "expired")
    public void defineExpiredGoods(SecKillGoodsItem secKillGoodsItem) {
        secKillGoodsItem.goodsTitle="TTTTT";
        secKillGoodsItem.secKillBeginAt = DateHelper.beforeDays(new Date(), 3);
        secKillGoodsItem.secKillEndAt = DateHelper.beforeDays(new Date(), 1);
    }
}
