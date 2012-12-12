package models;

import java.math.BigDecimal;
import java.util.List;

import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;

import javax.persistence.Query;

/**
 * 销售报表
 * <p/>
 * User: wangjia
 * Date: 12-12-11
 * Time: 下午4:54
 */
public class SalesReport {
    public Goods goods;
    public BigDecimal avgOriginalPrice;
    public BigDecimal salePrice;
    public Long buyNumber;
    public BigDecimal totalAmount;
    public String reportDate;

    public SalesReport(Goods goods, BigDecimal salePrice, Long buyNumber) {
        this.goods = goods;
        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
    }

    /**
     * 取得按商品统计的销售记录
     *
     * @param condition
     * @return
     */
    public static List<SalesReport> query(SalesReportCondition condition) {
        String sql = "select new models.SalesReport(e.orderItems.goods,e.salePrice,count(e.orderItems.buyNumber),sum(e.refundPrice)) from ECoupon e ";
        String groupBy = " group by e.orderItems.goods";

        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SalesReport> resultList = query.getResultList();
        return resultList;
    }

}
