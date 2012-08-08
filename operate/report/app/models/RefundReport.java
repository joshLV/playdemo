package models;

import models.sales.Goods;
import models.supplier.Supplier;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-7
 * Time: 下午4:05
 */
public class RefundReport extends Model {
    public String supplierName;
    public Goods goods;
    public BigDecimal salePrice;
    public Long buyNumber;
    public BigDecimal amount;
    public BigDecimal totalAmount;

    public RefundReport(Goods goods, BigDecimal salePrice, Long buyNumber) {
        this.goods = goods;
        if (goods != null) {
            Supplier supplier = Supplier.findById(goods.supplierId);
            this.supplierName = StringUtils.isNotBlank(supplier.otherName) ? supplier.otherName : supplier.fullName;
        }
        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
    }

    public RefundReport(long buyCount, BigDecimal amount, BigDecimal totAmount) {
        this.amount = amount;
        this.buyNumber = buyCount;
        this.totalAmount = totAmount;
    }

    public static List<RefundReport> query(RefundReportCondition condition) {

        String sql = "select new models.RefundReport(e.orderItems.goods,e.salePrice,count(e.orderItems.buyNumber)) from ECoupon e ";
        String groupBy = " group by e.orderItems.goods";

        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<RefundReport> resultList = query.getResultList();
        for (RefundReport refundReport : resultList) {
            refundReport.amount = refundReport.salePrice.multiply(new BigDecimal(refundReport.buyNumber));
        }
        return resultList;
    }

    public static RefundReport summary(List<RefundReport> resultList) {
        if (resultList.size() == 0) {
            return new RefundReport(0l, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        long buyCount = 0l;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal totAmount = BigDecimal.ZERO;
        for (RefundReport item : resultList) {
            buyCount += item.buyNumber;
            amount = amount.add(item.salePrice == null ? new BigDecimal(0) : item.salePrice);
            totAmount = totAmount.add(item.amount == null ? new BigDecimal(0) : item.amount);

        }
        return new RefundReport(buyCount, amount, totAmount);

    }
}

