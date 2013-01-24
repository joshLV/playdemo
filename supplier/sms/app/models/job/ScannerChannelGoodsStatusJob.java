package models.job;

import models.resale.Resaler;
import models.sales.ChannelGoodsInfo;
import models.sales.ChannelGoodsInfoStatus;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;

import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 每小时查询各渠道商品的状态
 * <p/>
 * User: yanjy
 * Date: 12-12-17
 * Time: 下午3:51
 */
@Every("2mn")
public class ScannerChannelGoodsStatusJob extends Job {

    @Override
    public void doJob() {
        List<Resaler> resalerList = Resaler.findByStatus(null);
        for (Resaler resaler : resalerList) {
            String onSaleKey = resaler.onSaleKey;
            String offSaleKey = resaler.offSaleKey;

            if (StringUtils.isBlank(onSaleKey) && StringUtils.isBlank(offSaleKey)) {
                continue;
            }

            Pattern onSalePattern = Pattern.compile(onSaleKey);
            Pattern offSalePattern = Pattern.compile(offSaleKey);

            String sql = "select c from ChannelGoodsInfo c where c.deleted=0 and c.resaler=:resaler and c.status =:status";
            Query query = JPA.em().createQuery(sql);
            query.setParameter("resaler", resaler);
            query.setParameter("status", ChannelGoodsInfoStatus.CREATED);
            query.setFirstResult(0);
            query.setMaxResults(200);
            List<ChannelGoodsInfo> resultList = query.getResultList();
            for (ChannelGoodsInfo channelGoodsInfo : resultList) {
                String url = channelGoodsInfo.url;
                //变更前的状态
                ChannelGoodsInfoStatus preStatus = channelGoodsInfo.status;
                WebServiceClient client = WebServiceClientFactory
                        .getClientHelper();
                String retResponse = client.getString("", url, resaler.id.toString());

                Matcher onSaleMatcher = onSalePattern.matcher(retResponse);
                Matcher offSaleMatcher = offSalePattern.matcher(retResponse);
                if (preStatus != ChannelGoodsInfoStatus.ONSALE && onSaleMatcher.find()) {
                    channelGoodsInfo.status = ChannelGoodsInfoStatus.ONSALE;
                    channelGoodsInfo.onSaleAt = new Date();
                    channelGoodsInfo.save();
                } else if (preStatus != ChannelGoodsInfoStatus.OFFSALE && (!onSaleMatcher.find() || offSaleMatcher.find())) {
                    channelGoodsInfo.status = ChannelGoodsInfoStatus.OFFSALE;
                    channelGoodsInfo.offSaleAt = new Date();
                    channelGoodsInfo.save();
                } else if (!offSaleMatcher.find() && !onSaleMatcher.find()) {
                    channelGoodsInfo.status = ChannelGoodsInfoStatus.UNKNOWN;
                    channelGoodsInfo.save();
                }

            }
        }
    }
}
