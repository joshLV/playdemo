package models;

import models.sales.Goods;
import models.sales.MaterialType;
import models.supplier.Supplier;
import org.h2.util.StringUtils;
import play.db.jpa.JPA;
import play.db.jpa.Model;

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
    public String goodsName;
    public MaterialType materialType;

    public Goods goods;
    public BigDecimal salePrice;
    public Long buyNumber;
    public BigDecimal amount;

    public RefundReport(Goods goods, BigDecimal salePrice, Long buyNumber) {
        this.goods = goods;
        if (goods != null) {
            this.goodsName = goods.name;
            Supplier supplier = Supplier.findById(goods.id);
            this.supplierName = StringUtils.isNullOrEmpty(supplier.otherName) ? supplier.fullName : supplier.otherName;

        }

        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
    }

    public static List<RefundReport> query(RefundReportCondition condition) {

        String sql = "select new models.RefundReport(e.orderItems.goods,sum(e.salePrice),count(e.orderItems.buyNumber)) from ECoupon e ";
        String groupBy = " group by e.orderItems.goods";

        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<RefundReport> resultList = query.getResultList();
        return resultList;
    }

    public static RefundReport summary(List<RefundReport> resultList) {
        return new RefundReport(new Goods(), BigDecimal.ZERO, 0l);
    }
}

