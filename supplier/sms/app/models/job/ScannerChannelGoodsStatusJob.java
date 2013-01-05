package models.job;

import models.resale.Resaler;
import models.sales.ChannelGoodsInfo;
import models.sales.GoodsStatus;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.WS;

import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * 每小时查询各渠道商品的状态
 * <p/>
 * User: yanjy
 * Date: 12-12-17
 * Time: 下午3:51
 */
@Every("1h")
public class ScannerChannelGoodsStatusJob extends Job {
    @Override
    public void doJob() {
        List<Resaler> resalerList = Resaler.findByStatus(null);
        for (Resaler resaler : resalerList) {
            String sql = "select c from ChannelGoodsInfo c where c.deleted=0 and c.resaler=:resaler";
            Query query = JPA.em().createQuery(sql);
            query.setParameter("resaler", resaler);
            query.setFirstResult(0);
            query.setMaxResults(200);
            List<ChannelGoodsInfo> resultList = query.getResultList();
            for (ChannelGoodsInfo channelGoodsInfo : resultList) {
                String url = channelGoodsInfo.url;
                //变更前的状态
                GoodsStatus preStatus = channelGoodsInfo.status;
                WS.HttpResponse response = WS.url(url).get();

                if (preStatus != GoodsStatus.ONSALE && StringUtils.isNotBlank(resaler.thirdStatusBuy) && response.getString().contains(resaler.thirdStatusBuy)) {
                    channelGoodsInfo.status = GoodsStatus.ONSALE;
                    channelGoodsInfo.onSaleAt = new Date();
                    channelGoodsInfo.offSaleAt = null;
                    channelGoodsInfo.save();
                } else if (preStatus != GoodsStatus.OFFSALE && StringUtils.isNotBlank(resaler.thirdStatusEnd) && response.getString().contains(resaler.thirdStatusEnd)) {
                    channelGoodsInfo.status = GoodsStatus.OFFSALE;
                    channelGoodsInfo.offSaleAt = new Date();
                    channelGoodsInfo.onSaleAt = null;
                    channelGoodsInfo.save();
                }

            }
        }


    }

}
