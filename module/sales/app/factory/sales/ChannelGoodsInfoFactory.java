package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.resale.Resaler;
import models.sales.ChannelGoodsInfo;
import models.sales.Goods;

/**
 * <p/>
 * User: yanjy
 * Date: 13-1-5
 * Time: 下午4:45
 */
public class ChannelGoodsInfoFactory extends ModelFactory<ChannelGoodsInfo> {
    @Override
    public ChannelGoodsInfo define() {
        Goods goods = FactoryBoy.create(Goods.class);
        Resaler resaler = FactoryBoy.create(Resaler.class);
        ChannelGoodsInfo channelGoodsInfo = new ChannelGoodsInfo(goods, resaler, "http://yibaiquan.com/p/3", "", "");
        return channelGoodsInfo;
    }

    @Factory(name = "jingdong")
    public ChannelGoodsInfo defineWithJD(ChannelGoodsInfo goodsInfo) {
        goodsInfo.url = "http://yibaiquan.com/p/1";
        return goodsInfo;
    }

    @Factory(name = "wuba")
    public ChannelGoodsInfo defineWuba(ChannelGoodsInfo goodsInfo) {
        goodsInfo.url = "http://yibaiquan.com/p/2";
        return goodsInfo;
    }
}
