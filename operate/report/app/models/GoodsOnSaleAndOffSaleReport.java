package models;

import models.resale.Resaler;
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

    public static CrossTableConverter<GoodsOnSaleAndOffSaleReport, String> converter = new CrossTableConverter<GoodsOnSaleAndOffSaleReport, String>() {
        @Override
        public String getRowKey(GoodsOnSaleAndOffSaleReport target) {
            return target.goods.shortName + "-" + target.goods.code;
        }

        @Override
        public String getColumnKey(GoodsOnSaleAndOffSaleReport target) {
            return target.resaler.userName;
        }

        @Override
        public String addValue(GoodsOnSaleAndOffSaleReport target, String oldValue) {
            if (target.url == null) {
                return oldValue;
            }
            if (oldValue != null) {
                target.url = oldValue;
            }
            return target.url;
        }
    };

    public GoodsOnSaleAndOffSaleReport(Resaler resaler) {
        this.resalerName = resaler.userName;
    }

    public GoodsOnSaleAndOffSaleReport(Goods goods, Resaler resaler, String url) {
        this.goods = goods;
        this.resaler = resaler;
        this.url = url;
    }

    public static List<GoodsOnSaleAndOffSaleReport> findByStatus() {
        Query query = JPA.em()
                .createQuery(
                        "select new models.GoodsOnSaleAndOffSaleReport(c.resaler) "
                                + " from ChannelGoodsInfo c "
                                + " group by  c.resaler order by c.resaler desc");

        return query.getResultList();
    }

    public static List<GoodsOnSaleAndOffSaleReport> getChannelGoods(GoodsOnSaleAndOffSaleCondition condition) {
        Query query = JPA.em()
                .createQuery(
                        "select new models.GoodsOnSaleAndOffSaleReport(c.goods,c.resaler,c.url) "
                                + " from ChannelGoodsInfo c "
                                + condition.filter() + " group by c.goods, c.resaler order by c.resaler desc");

        for (Map.Entry<String, Object> param : condition.getParamMap().entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        return query.getResultList();
    }


}
