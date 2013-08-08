package models;

import models.order.Order;
import models.resale.Resaler;
import models.sales.Goods;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

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

    public Order order;

    public String userName;
    public String loginName;
    public Long resalerId;

    public BigDecimal xjAmount;
    public BigDecimal xjRefundPrice;
    public BigDecimal xjSaleCost;
    public BigDecimal xjRefundCost;
    public BigDecimal xjNetSalesAmount;

    Long xjNumber = 0L;
    Long xjRefundNumber = 0L;
    public String code;

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

    /**
     * 渠道，售出/退款订单
     */
    public RealGoodsSalesReport(Order order, Goods goods, BigDecimal salesPrice, BigDecimal originalPrice, Long count, String saleFlag) {
        this.goods = goods;
        this.salesPrice = salesPrice;
        this.originalPrice = originalPrice;
        this.order = order;
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
        if (order != null) {
            this.loginName = order.getResaler().loginName;
            this.userName = order.getResaler().userName;
            this.resalerId = order.getResaler().id;
        }

    }

    public RealGoodsSalesReport(BigDecimal salesAmount, BigDecimal refundAmount, BigDecimal netSalesAmount) {
        this.salesAmount = salesAmount;
        this.refundAmount = refundAmount;
        this.netSalesAmount = netSalesAmount;
    }

    public RealGoodsSalesReport() {
        //To change body of created methods use File | Settings | File Templates.
    }

    public static List<RealGoodsSalesReport> findByCondition(RealGoodsSalesReportCondition condition) {
        //售出
        String sql = "select new models.RealGoodsSalesReport(r.goods,r.salePrice,r.originalPrice,sum(r.buyNumber),'1')" +
                " from OrderItems r ";
        String groupBy = " group by r.goods.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(r.buyNumber)*r.salePrice desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<RealGoodsSalesReport> paidResultList = query.getResultList();


        //退款
        sql = "select new models.RealGoodsSalesReport(r.goods,r.salePrice,r.originalPrice,sum(r.returnCount),'0')" +
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
        }
        netSalesAmount = netSalesAmount.add(totalAmount.subtract(refundAmount));
        return new RealGoodsSalesReport(totalAmount, refundAmount, netSalesAmount);
    }

    /**
     * 渠道实物销售统计
     */
    public static RealGoodsSalesReport getChannelNetSummary(List<RealGoodsSalesReport> realGoodsOrderList) {
        if (realGoodsOrderList.size() == 0) {
            return new RealGoodsSalesReport(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        BigDecimal netSalesAmount = BigDecimal.ZERO;
        for (RealGoodsSalesReport item : realGoodsOrderList) {
            totalAmount = totalAmount.add(item.salesAmount == null ? BigDecimal.ZERO : item.salesAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
        }
        netSalesAmount = netSalesAmount.add(totalAmount.subtract(refundAmount));
        return new RealGoodsSalesReport(totalAmount, refundAmount, netSalesAmount);
    }
    public static List<RealGoodsSalesReport> findChannleSales(RealGoodsSalesReportCondition condition) {
        //售出
        String sql = "select new models.RealGoodsSalesReport(r.order,r.goods,r.salePrice,r.originalPrice,sum(r.buyNumber),'1')" +
                " from OrderItems r ";
        String groupBy = " group by r.order.userId,r.goods.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by r.order.userId ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<RealGoodsSalesReport> paidResultList = query.getResultList();

        //退款
        sql = "select new models.RealGoodsSalesReport(r.order,r.goods,r.salePrice,r.originalPrice,sum(r.returnCount),'0')" +
                " from OrderItems r ,RealGoodsReturnEntry  rr";
        groupBy = " group by r.order.userId,r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter() + groupBy + " order by r.order.userId ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<RealGoodsSalesReport> refundResultList = query.getResultList();
        //merge the same goods data
        Map<String, RealGoodsSalesReport> goodsDataMap = new HashMap<>();
        for (RealGoodsSalesReport paidItem : paidResultList) {
            goodsDataMap.put(getReportKey(paidItem), paidItem);
        }
        for (RealGoodsSalesReport refundItem : refundResultList) {
            RealGoodsSalesReport item = goodsDataMap.get(getReportKey(refundItem));
            if (item == null) {
                goodsDataMap.put(getReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.refundCount = refundItem.refundCount;
                item.totalRefundCost = refundItem.totalRefundCost;
            }
        }


        //total
        RealGoodsSalesReport reportItem = null;
        List<RealGoodsSalesReport> resultList = new ArrayList();
        Set<Resaler> partnerSet = new HashSet<>();
        for (String s : goodsDataMap.keySet()) {
            reportItem = goodsDataMap.get(s);
            if (reportItem == null) {
                continue;
            }

            partnerSet.add(reportItem.order.getResaler());
            resultList.add(reportItem);

        }

        for (Resaler resaler : partnerSet) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalRefundPrice = BigDecimal.ZERO;
            BigDecimal totalCost = BigDecimal.ZERO;
            BigDecimal totalRefundCost = BigDecimal.ZERO;
            BigDecimal netSalesAmount = BigDecimal.ZERO;

            Long totalNumber = 0L;
            Long totalRefundNumber = 0L;
            for (RealGoodsSalesReport item : resultList) {
                if (resaler.id.equals(item.resalerId)) {
                    totalAmount = totalAmount.add(item.salesAmount == null ? BigDecimal.ZERO : item.salesAmount);
                    netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
                    totalRefundPrice = totalRefundPrice.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
                    totalNumber = totalNumber + (item.salesCount == null ? 0L : item.salesCount);
                    totalRefundNumber = totalRefundNumber + (item.refundCount == null ? 0L : item.refundCount);
                    totalCost = totalCost.add(item.totalSalesCost == null ? BigDecimal.ZERO : item.totalSalesCost);
                    totalRefundCost = totalRefundCost.add(item.totalRefundCost == null ? BigDecimal.ZERO : item.totalRefundCost);
                }
            }

            RealGoodsSalesReport report = new RealGoodsSalesReport();
            report.xjAmount = totalAmount;
            report.xjNetSalesAmount = netSalesAmount;
            report.xjNumber = totalNumber;
            report.xjRefundCost = totalRefundCost;
            report.xjRefundNumber = totalRefundNumber;
            report.xjSaleCost = totalCost;
            report.xjRefundPrice = totalRefundPrice;
            report.code = "999";
            report.resalerId = resaler.id;
            resultList.add(report);
        }
        sort(resultList);
        return resultList;
    }

    public static void sort(List<RealGoodsSalesReport> resultList) {
        Collections.sort(resultList, new Comparator<RealGoodsSalesReport>() {
            @Override
            public int compare(RealGoodsSalesReport o1, RealGoodsSalesReport o2) {

                return o1.resalerId.intValue() - o2.resalerId.intValue();

            }
        });
    }

    public static String getReportKey(RealGoodsSalesReport item) {
        if (item.order == null) {
            return String.valueOf(item.goods.id);
        } else {
            return String.valueOf(item.order.userId) + String.valueOf(item.goods.id);
        }
    }

}

