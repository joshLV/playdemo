package models;

import models.resale.Resaler;
import models.sales.ChannelGoodsInfoStatus;
import models.sales.Goods;
import play.db.jpa.JPA;
import utils.CrossTableConverter;

import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-21
 * Time: 下午4:37
 */
public class GoodsOnSaleAndOffSaleReport {
    public Goods goods;
    public Resaler resaler;
    public String url;
    public String resalerName;
    public ChannelGoodsInfoStatus status;
    public Long resalerId;
    public static CrossTableConverter<GoodsOnSaleAndOffSaleReport, GoodsOnSaleAndOffSaleReport> converter = new CrossTableConverter<GoodsOnSaleAndOffSaleReport, GoodsOnSaleAndOffSaleReport>() {
        @Override
        public String getRowKey(GoodsOnSaleAndOffSaleReport target) {
            return target.goods.shortName + "※" + target.goods.code + "※" + target.goods.id + "※" + target.goods.getStatus();
        }

        @Override
        public String getColumnKey(GoodsOnSaleAndOffSaleReport target) {
            return target.resaler.userName;
        }

        @Override
        public GoodsOnSaleAndOffSaleReport addValue(GoodsOnSaleAndOffSaleReport target, GoodsOnSaleAndOffSaleReport oldValue) {
            if (target == null) {
                return oldValue;
            }
            if (oldValue != null && oldValue.status == ChannelGoodsInfoStatus.ONSALE) {
                return oldValue;
            }
            if (target.status == ChannelGoodsInfoStatus.ONSALE) {
                return target;
            }
            return target;
        }
    };

    public GoodsOnSaleAndOffSaleReport(Resaler resaler) {
        this.resalerName = resaler.userName;
        this.resalerId = resaler.id;
    }

    public GoodsOnSaleAndOffSaleReport(Goods goods, Resaler resaler, String url, ChannelGoodsInfoStatus status) {
        this.goods = goods;
        this.resaler = resaler;
        this.url = url;
        this.status = status;
    }

    public static List<GoodsOnSaleAndOffSaleReport> findByStatus(GoodsOnSaleAndOffSaleCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.GoodsOnSaleAndOffSaleReport(c.resaler) "
                                + " from ChannelGoodsInfo c  "
                                + " group by c.resaler order by c.resaler desc");
        for (Map.Entry<String, Object> param : condition.getParamMap().entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        return query.getResultList();
    }

    public static List<GoodsOnSaleAndOffSaleReport> getChannelGoods(Long operateUserId, GoodsOnSaleAndOffSaleCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.GoodsOnSaleAndOffSaleReport(c.goods,c.resaler,c.url,c.status ) "
                                + " from ChannelGoodsInfo c "
                                + condition.filter(operateUserId) + " order by c.resaler desc");

        for (Map.Entry<String, Object> param : condition.getParamMap().entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }

        List<GoodsOnSaleAndOffSaleReport> onlyOnSaleList = query.getResultList();
        return onlyOnSaleList;
    }

    public static String getKey(GoodsOnSaleAndOffSaleReport item) {
        return String.valueOf(item.goods.id + item.resaler.id);
    }
}
