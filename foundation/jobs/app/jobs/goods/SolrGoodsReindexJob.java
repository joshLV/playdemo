package jobs.goods;

import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.sales.Goods;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.jobs.Every;
import play.modules.solr.Solr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Solr商品增量重建索引.
 * <p/>
 * User: sujie
 * Date: 12/17/12
 * Time: 1:47 PM
 */
@JobDefine(title="Solr商品增量重建索引", description="Solr商品增量重建索引")
@Every("5mn")
public class SolrGoodsReindexJob extends JobWithHistory {

    @Override
    public void doJobWithHistory() throws Exception {
        // 测试环境不执行此代码
        if (Play.runingInTestMode()) {
            return;
        }
        Calendar endDate = Calendar.getInstance();
        Calendar beginDate = Calendar.getInstance();
        beginDate.add(Calendar.MINUTE, -10);
        List<Goods> updatedGoods = Goods.findUpdatedGoods(beginDate.getTime(), endDate.getTime());
        if (CollectionUtils.isEmpty(updatedGoods)) {
            return;
        }

        List<String> goodsIds = new ArrayList<>();
        for (Goods updatedGood : updatedGoods) {
            Solr.save(updatedGood);
            goodsIds.add(updatedGood.id.toString());
        }
        Logger.info("Solr Goods reindex:(id:" + StringUtils.join(goodsIds, ",") + ")");
    }

}
