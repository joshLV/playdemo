package models;

import models.ktv.KtvProductGoods;
import models.ktv.KtvTaobaoUtil;
import play.jobs.Job;
import play.jobs.On;

import java.util.List;

/**
 * 每天凌晨三点执行，把当天之前的ktv sku 删除，同时更新最新的sku信息
 * User: yan
 * Date: 13-5-8
 * Time: 下午1:48
 */
@On("0 0 3 * * ?")
public class KtvUpdateSkuJob extends Job {
    @Override
    public void doJob() {
        List<KtvProductGoods> ktvProductGoodsList = KtvProductGoods.findAll();
        for (KtvProductGoods productGoods : ktvProductGoodsList) {
            KtvTaobaoUtil.updateTaobaoSkuByProductGoods(productGoods);
        }
    }
}
