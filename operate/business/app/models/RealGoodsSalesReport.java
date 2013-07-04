package models;

import models.sales.Goods;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: yan
 * Date: 13-7-3
 * Time: 下午4:31
 */
public class RealGoodsSalesReport {

    /**
     * 商品信息
     */
    public Goods goods;

    /**
     * 销售单价
     */
    public BigDecimal salesPrice;

    /**
     * 进价
     */
    public BigDecimal originalPrice;

    /**
     * 销售数量
     */
    public Long salesCount;

    /**
     * 总销售额
     */
    public BigDecimal salesAmount;

    /**
     * 总销售成本
     */
    public BigDecimal totalSalesCost;

    /**
     * 退款总额
     */
    public BigDecimal refundAmount;

    /**
     * 退款总成本
     */
    public BigDecimal totalRefundCost;
    /**
     * 退款数量
     */
    public Long refundCount;

    /**
     * 净销售额
     */
    public BigDecimal netSalesAmount;


    /**
     * 售出/退款订单
     */
    public RealGoodsSalesReport(Goods goods, BigDecimal salesPrice, BigDecimal originalPrice, Long count, String saleFlag) {
        this.goods = goods;
        this.salesPrice = salesPrice;
        this.originalPrice = originalPrice;
        BigDecimal tempCount = new BigDecimal(count);
        if ("1".equals(saleFlag)) {
            this.salesAmount = salesPrice.multiply(tempCount);
            this.totalSalesCost = originalPrice.multiply(tempCount);
            this.salesCount = count;
        } else {
            this.refundAmount = salesPrice.multiply(tempCount);
            this.totalRefundCost = originalPrice.multiply(tempCount);
            this.refundCount = count;
        }
    }

    public RealGoodsSalesReport(BigDecimal salesAmount, BigDecimal refundAmount, BigDecimal netSalesAmount) {
        this.salesAmount = salesAmount;
        this.refundAmount = refundAmount;
        this.netSalesAmount = netSalesAmount;
    }

    public static List<RealGoodsSalesReport> findByCondition(RealGoodsSalesReportCondition condition) {
        //售出
        String sql = "select new models.RealGoodsSalesReport(r.goods,r.goods.salePrice,r.goods.originalPrice,sum(r.buyNumber),'1')" +
                " from OrderItems r ";
        String groupBy = " group by r.goods.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(r.buyNumber)*r.salePrice desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<RealGoodsSalesReport> paidResultList = query.getResultList();


        //退款
        sql = "select new models.RealGoodsSalesReport(r.goods,r.goods.salePrice,r.goods.originalPrice,sum(r.returnCount),'0')" +
                " from OrderItems r ,RealGoodsReturnEntry  rr";
        groupBy = " group by r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by sum(r.buyNumber)*r.salePrice desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<RealGoodsSalesReport> refundResultList = query.getResultList();
        //merge the same goods data
        Map<Goods, RealGoodsSalesReport> goodsDataMap = new HashMap<>();
        for (RealGoodsSalesReport paidItem : paidResultList) {
            goodsDataMap.put(paidItem.goods, paidItem);
        }

        for (RealGoodsSalesReport refundItem : refundResultList) {
            RealGoodsSalesReport item = goodsDataMap.get(refundItem.goods);
            if (item == null) {
                goodsDataMap.put(refundItem.goods, refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.refundCount = refundItem.refundCount;
                item.totalRefundCost = refundItem.totalRefundCost;
            }
        }

        List resultList = new ArrayList();
        for (Goods key : goodsDataMap.keySet()) {
            resultList.add(goodsDataMap.get(key));
        }
        return resultList;

    }

    /**
     * 统计
     */
    public static RealGoodsSalesReport getNetSummary(List<RealGoodsSalesReport> realGoodsOrderList) {
        if (realGoodsOrderList.size() == 0) {
            return new RealGoodsSalesReport(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        BigDecimal netSalesAmount = BigDecimal.ZERO;
        for (RealGoodsSalesReport item : realGoodsOrderList) {
            totalAmount = totalAmount.add(item.salesAmount == null ? BigDecimal.ZERO : item.salesAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
            netSalesAmount = netSalesAmount.add(totalAmount.subtract(refundAmount));
        }
        return new RealGoodsSalesReport(totalAmount, refundAmount, netSalesAmount);
    }
}

