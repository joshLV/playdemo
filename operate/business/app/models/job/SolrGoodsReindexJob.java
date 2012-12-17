package models.job;

import models.sales.Goods;
import org.apache.commons.collections.CollectionUtils;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.modules.solr.Solr;

import java.util.Calendar;
import java.util.List;

/**
 * Solr商品增量重建索引.
 * <p/>
 * User: sujie
 * Date: 12/17/12
 * Time: 1:47 PM
 */
@Every("5mn")
public class SolrGoodsReindexJob extends Job {

    @Override
    public void doJob() throws Exception {
        Calendar endDate = Calendar.getInstance();
        Calendar beginDate = Calendar.getInstance();
        beginDate.add(Calendar.MINUTE, -5);
        List<Goods> updatedGoods = Goods.findUpdatedGoods(endDate, beginDate);
        if (CollectionUtils.isEmpty(updatedGoods)) {
            return;
        }

        StringBuilder goodsIds = new StringBuilder();
        for (Goods updatedGood : updatedGoods) {
            Solr.save(updatedGood);
            goodsIds.append(updatedGood.id);
            goodsIds.append(",");
        }
        Logger.info("Solr Goods reindex:(id:" + goodsIds + ")");
    }

}
