package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.sales.Goods;
import models.sales.MaterialType;
import models.sales.SecKillGoods;

import java.math.BigDecimal;
import java.util.Date;

import static util.DateHelper.afterDays;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-16
 * Time: 上午10:41
 */
public class SecKillGoodsFactory extends ModelFactory<SecKillGoods> {

    @Override
    public SecKillGoods define() {
        Goods goods = FactoryBoy.create(Goods.class);
        SecKillGoods secKillGoods = new SecKillGoods();
        secKillGoods.personLimitNumber= 1;
        secKillGoods.setPrompt("wowuroqwl");
        secKillGoods.goods = goods;
        secKillGoods.imagePath = "/a.jpg";
        secKillGoods.createdAt= afterDays(new Date(), 30);
        secKillGoods.goods.faceValue=new BigDecimal(10);


        return secKillGoods;
    }


    @Factory(name = "exceedLimit")
    public SecKillGoods defineWithExceedLimit(SecKillGoods secKillGoods){
        Goods goods = FactoryBoy.create(Goods.class);
        secKillGoods = new SecKillGoods();
        secKillGoods.personLimitNumber= 1;
        secKillGoods.setPrompt("wowuroqwl");
        secKillGoods.goods = goods;
        secKillGoods.imagePath = "/a.jpg";
        secKillGoods.createdAt= afterDays(new Date(), 30);
        secKillGoods.goods.faceValue=new BigDecimal(10);

        return secKillGoods;

    }
}
