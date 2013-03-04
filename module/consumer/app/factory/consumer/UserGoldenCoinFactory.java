package factory.consumer;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.consumer.User;
import models.consumer.UserGoldenCoin;
import models.sales.Goods;

import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-17
 * Time: 下午5:09
 */
public class UserGoldenCoinFactory extends ModelFactory<UserGoldenCoin> {
    @Override
    public UserGoldenCoin define() {
        User user = FactoryBoy.lastOrCreate(User.class);
        UserGoldenCoin goldenCoin = new UserGoldenCoin();
        goldenCoin.checkinNumber = 5L;
        goldenCoin.user = user;
        goldenCoin.goods = FactoryBoy.create(Goods.class);
        goldenCoin.remarks = "每天签到";
        goldenCoin.createdAt = new Date();
        return goldenCoin;
    }

    @Factory(name = "duihuan")
    public UserGoldenCoin duihuan(UserGoldenCoin goldenCoin) {
        goldenCoin.checkinNumber = -1000L;
        goldenCoin.remarks = "兑换抵用券";
        return goldenCoin;
    }

    @Factory(name = "jl")
    public UserGoldenCoin jl(UserGoldenCoin goldenCoin) {
        goldenCoin.checkinNumber = 100l;
        goldenCoin.remarks = "奖励：100金币";
        return goldenCoin;
    }
}
