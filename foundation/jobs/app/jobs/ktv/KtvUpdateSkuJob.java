package jobs.ktv;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.ktv.KtvProductGoods;
import models.ktv.KtvTaobaoUtil;
import models.taobao.KtvSkuMessageUtil;
import play.jobs.Job;
import play.jobs.On;

import java.util.List;

/**
 * User: yan
 * Date: 13-5-8
 * Time: 下午1:48
 */
@JobDefine(title = "更新淘宝ktv sku", description = "每天18点执行，把当天之前的ktv sku 删除，同时更新最新的sku信息")
@On("0 0/10 18,19 * * ?")
public class KtvUpdateSkuJob extends JobWithHistory {
    @Override
    public void doJobWithHistory() {
        List<KtvProductGoods> ktvProductGoodsList = KtvProductGoods.findAll();
        for (KtvProductGoods productGoods : ktvProductGoodsList) {
            KtvSkuMessageUtil.send(productGoods.id);
        }
    }
}
