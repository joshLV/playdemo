package jobs.ktv;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.ktv.KtvProductGoods;
import models.ktv.KtvTaobaoUtil;
import play.jobs.Job;
import play.jobs.On;

import java.util.List;

/**
 * User: yan
 * Date: 13-5-8
 * Time: 下午1:48
 */
@JobDefine(title = "更新淘宝ktv sku", description = "每天凌晨三点执行，把当天之前的ktv sku 删除，同时更新最新的sku信息")
@On("0 0 3 * * ?")
public class KtvUpdateSkuJob extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        List<KtvProductGoods> ktvProductGoodsList = KtvProductGoods.findAll();
        for (KtvProductGoods productGoods : ktvProductGoodsList) {
            KtvTaobaoUtil.updateTaobaoSkuByProductGoods(productGoods);
        }
    }
}
