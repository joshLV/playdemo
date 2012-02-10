package service;

import models.sales.Goods;
import play.modules.sales.GoodsService;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 2/9/12
 * Time: 1:49 PM
 */
public class GoodsServiceImpl implements GoodsService {
    @Override
    public void addGoods(Object context) {

        System.out.println("context:" + context);
        if (context instanceof Goods) {
            Goods goods = (Goods) context;
            goods.create();
        }
    }
}
