package factory.order;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.consumer.User;
import models.order.Cart;
import models.sales.Goods;
import util.DateHelper;

/**
 * User: wangjia
 * Date: 12-10-30
 * Time: 下午2:20
 */
public class CartFactory extends ModelFactory<Cart> {
    @Override
    public Cart define() {
        Goods goods = FactoryBoy.lastOrCreate(Goods.class);
        Cart cart = new Cart(goods, 1);
        cart.user = FactoryBoy.lastOrCreate(User.class);
        cart.createdAt = DateHelper.t("2012-05-28 15:00:48");
        cart.lockVersion = 0;
        cart.updatedAt = DateHelper.t("2012-05-28 15:00:48");
        return cart;

    }
}
