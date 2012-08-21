package factory.sales;

import models.sales.Goods;
import models.sales.SecKillGoods;
import factory.FactoryBoy;
import factory.ModelFactory;

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
        return secKillGoods;
    }
}
