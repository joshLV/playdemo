package factory.sales;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.resale.Resaler;
import models.sales.ChannelGoodsInfo;
import models.sales.ChannelGoodsInfoStatus;
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
        Goods goods = FactoryBoy.lastOrCreate(Goods.class);
        Resaler resaler = FactoryBoy.lastOrCreate(Resaler.class);
        ChannelGoodsInfo channelGoodsInfo = new ChannelGoodsInfo(goods, resaler, "http://yibaiquan.com/p/3", "", "");
        channelGoodsInfo.status = ChannelGoodsInfoStatus.ONSALE;
        return channelGoodsInfo;
    }

    @Factory(name = "jingdong")
    public void defineWithJD(ChannelGoodsInfo goodsInfo) {
        goodsInfo.url = "http://yibaiquan.com/p/1";
    }

    @Factory(name = "wuba")
    public void defineWuba(ChannelGoodsInfo goodsInfo) {
        goodsInfo.url = "http://yibaiquan.com/p/2";
    }
}
