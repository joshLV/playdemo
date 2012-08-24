package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.sales.SecKillGoods;
import models.sales.SecKillGoodsItem;
import models.sales.SecKillGoodsStatus;
import util.DateHelper;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        Date date = DateHelper.beforeMinuts(new Date(), 100);
        Date dateAfter=DateHelper.afterMinuts(new Date(), 10);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SecKillGoodsItem secKillGoodsItem = new SecKillGoodsItem();
        secKillGoodsItem.virtualInventory = 1l;
        secKillGoodsItem.goodsTitle = "第一波秒杀";
        secKillGoodsItem.saleCount = 0;
        secKillGoodsItem.salePrice = new BigDecimal(10);
        try {
            secKillGoodsItem.secKillBeginAt = dateFormat.parse(dateFormat.format(date));
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        try {
            secKillGoodsItem.secKillEndAt = dateFormat.parse(dateFormat.format(dateAfter));


        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        SecKillGoods goods= FactoryBoy.create(SecKillGoods.class);
        secKillGoodsItem.secKillGoods=goods;


        secKillGoodsItem.baseSale =100l;
        secKillGoodsItem.status= SecKillGoodsStatus.ONSALE;




        return secKillGoodsItem;


    }

    @Factory(name = "expired")
    public void defineExpiredGoods(SecKillGoodsItem secKillGoodsItem) {
        secKillGoodsItem.goodsTitle="TTTTT";
        secKillGoodsItem.secKillBeginAt = DateHelper.beforeDays(new Date(), 3);
        secKillGoodsItem.secKillEndAt = DateHelper.beforeDays(new Date(), 1);
    }

    @Factory(name = "error")
    public SecKillGoodsItem defineWithError(SecKillGoodsItem secKillGoodsItem) {
        secKillGoodsItem.virtualInventory = 1l;
        secKillGoodsItem.goodsTitle = "第一波秒杀";
        secKillGoodsItem.saleCount = 0;
        secKillGoodsItem.salePrice = new BigDecimal(10);
        secKillGoodsItem.secKillBeginAt = new Date();
        secKillGoodsItem.secKillEndAt = DateHelper.afterMinuts(new Date(), 10);
        SecKillGoods goods= FactoryBoy.create(SecKillGoods.class);
        secKillGoodsItem.secKillGoods=goods;
        secKillGoodsItem.baseSale =1000l;
        secKillGoodsItem.status= SecKillGoodsStatus.ONSALE;
        return secKillGoodsItem;
    }

}
