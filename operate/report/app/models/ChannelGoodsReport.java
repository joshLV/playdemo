package models;

import models.accounts.AccountType;
import models.order.Order;
import models.sales.Goods;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-12-25
 * Time: 上午10:16
 * To change this template use File | Settings | File Templates.
 */
public class ChannelGoodsReport {
    public Order order;
    public String loginName;
    public String userName;
    public Goods goods;
    public BigDecimal avgSalesPrice;

    /**
     * 毛利率
     */
    public BigDecimal grossMargin;
    public BigDecimal originalPrice;
    public Long buyNumber;
    public BigDecimal totalAmount;
    public String reportDate;
    public BigDecimal refundAmount;
    public BigDecimal profit;
    public BigDecimal netSalesAmount;
    public BigDecimal totalCost;
    public BigDecimal ratio;
    public BigDecimal originalAmount;

    public ChannelGoodsReport(Order order, Goods goods, BigDecimal originalPrice, Long buyNumber,
                              BigDecimal totalAmount, BigDecimal avgSalesPrice,
                              BigDecimal grossMargin, BigDecimal profit, BigDecimal netSalesAmount) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.goods = goods;
        this.originalPrice = originalPrice;
        this.buyNumber = buyNumber;
        this.totalAmount = totalAmount;
        this.avgSalesPrice = avgSalesPrice;
        this.grossMargin = grossMargin;
        this.profit = profit;
        this.netSalesAmount = netSalesAmount;
    }

    //from resaler
    public ChannelGoodsReport(Order order, Goods goods, BigDecimal totalAmount, BigDecimal totalCost, BigDecimal profit, BigDecimal ratio) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.goods = goods;
        this.totalAmount = totalAmount;
        this.totalCost = totalCost;
        this.profit = profit;
        this.ratio = ratio;
    }

    //refund ecoupon
    public ChannelGoodsReport(Order order, BigDecimal refundAmount, Goods goods) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.refundAmount = refundAmount;
        this.goods = goods;

    }

    public ChannelGoodsReport(Order order, Long buyNumber, BigDecimal originalAmount) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.buyNumber = buyNumber;
        this.originalAmount = originalAmount;
    }

    public ChannelGoodsReport(BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal netSalesAmount) {
        this.totalAmount = totalAmount;
        this.netSalesAmount = netSalesAmount;
        this.refundAmount = refundAmount;
    }

    /**
     * 取得按商品统计的销售记录
     *
     * @param condition
     * @return
     */
    public static List<ChannelGoodsReport> query(ChannelGoodsReportCondition condition) {
        //paidAt
        String sql = "select new models.ChannelGoodsReport(r.order, r.goods,r.originalPrice,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/sum(r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-r.originalPrice*sum(r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-r.originalPrice*sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue))" +
                " from OrderItems r,Order o where r.order=o and ";
        String groupBy = " group by  r.order.userId, r.goods.id ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter(AccountType.RESALER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResultList = query.getResultList();

//        System.out.println("padiRe>>>" + paidResultList.size());
//
//        for (ChannelGoodsReport c : paidResultList) {
//            System.out.println("c.name>>" + c.loginName);
//            System.out.println("c.goods.name>>>" + c.goods.name);
//            System.out.println("c.buy>>>" + c.buyNumber);
//            System.out.println("");
//        }


        //from resaler
        sql = "select new models.ChannelGoodsReport(r.order, r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue),r.originalPrice*sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-r.originalPrice*sum(r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b where r.order=o and  ";
        groupBy = " group by r.order.userId, r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter(AccountType.RESALER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResalerResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.ChannelGoodsReport(e.order, sum(e.refundPrice),e.orderItems.goods) " +
                " from ECoupon e ";
        groupBy = " group by e.order.userId, e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter(AccountType.RESALER) + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<ChannelGoodsReport> refundList = query.getResultList();

        Map<String, ChannelGoodsReport> map = new HashMap<>();

        //merge
        for (ChannelGoodsReport paidItem : paidResultList) {
            System.out.println("ffffffffffffffffffff");
            System.out.println("padiitem.name>>>" + paidItem.loginName);
            System.out.println("getReportKey(paidItem)>>>" + getReportKey(paidItem));
            System.out.println("");
            map.put(getReportKey(paidItem), paidItem);
        }

        for (ChannelGoodsReport refundItem : refundList) {
            ChannelGoodsReport item = map.get(getReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                map.put(getReportKey(refundItem), refundItem);
            } else {
                System.out.println("refundItem.name>>>" + refundItem.loginName);
                System.out.println("getReportKey(refundItem)>>>" + getReportKey(refundItem));
                item.refundAmount = refundItem.refundAmount;
                item.netSalesAmount = item.totalAmount.subtract(item.refundAmount);
            }
        }

        //merge from resaler if commissionRatio
        for (ChannelGoodsReport resalerItem : paidResalerResultList) {
            ChannelGoodsReport item = map.get(getReportKey(resalerItem));
            if (item == null) {
                map.put(getReportKey(resalerItem), resalerItem);
            } else {
                System.out.println("resalerItem.name>>>" + resalerItem.loginName);
                System.out.println("getReportKey(resalerItem)>>>" + getReportKey(resalerItem));
                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.totalCost);
            }
        }

        List resultList = new ArrayList();

        List<String> tempString = new ArrayList<>();
        for (String s : map.keySet()) {
            tempString.add(s);

        }
        Collections.sort(tempString);

        for (String key : tempString) {
            resultList.add(map.get(key));
        }


        return resultList;
    }


    /**
     * 取得按商品统计的销售记录     consumer
     *
     * @param condition
     * @return
     */
    public static List<ChannelGoodsReport> queryConsumer(ChannelGoodsReportCondition condition) {
        //paidAt
        String sql = "select new models.ChannelGoodsReport(r.order, r.goods,r.originalPrice,sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)/sum(r.buyNumber)" +
                ",(sum(r.salePrice*r.buyNumber-r.rebateValue)-r.originalPrice*sum(r.buyNumber))/sum(r.salePrice*r.buyNumber-r.rebateValue)*100" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)-r.originalPrice*sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue))" +
                " from OrderItems r,Order o where r.order=o and ";
        String groupBy = " group by  r.goods.id ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResultList = query.getResultList();

//        System.out.println("padiRe>>>" + paidResultList.size());
//
//        for (ChannelGoodsReport c : paidResultList) {
//            System.out.println("c.name>>" + c.loginName);
//            System.out.println("c.goods.name>>>" + c.goods.name);
//            System.out.println("c.buy>>>" + c.buyNumber);
//            System.out.println("");
//        }


        //from resaler
        sql = "select new models.ChannelGoodsReport(r.order, r.goods,sum(r.salePrice*r.buyNumber-r.rebateValue),r.originalPrice*sum(r.buyNumber)" +
                ",sum(r.salePrice*r.buyNumber-r.rebateValue)*(1-b.commissionRatio/100)-r.originalPrice*sum(r.buyNumber)" +
                ",b.commissionRatio)" +
                " from OrderItems r,Order o,Resaler b where r.order=o and  ";
        groupBy = " group by  r.goods.id";
        query = JPA.em()
                .createQuery(sql + condition.getResalerFilter(AccountType.CONSUMER) + groupBy + " order by sum(r.salePrice-r.rebateValue) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ChannelGoodsReport> paidResalerResultList = query.getResultList();

        //取得退款的数据 ecoupon
        sql = "select new models.ChannelGoodsReport(e.order, sum(e.refundPrice),e.orderItems.goods) " +
                " from ECoupon e ";
        groupBy = " group by e.orderItems.goods.id";

        query = JPA.em()
                .createQuery(sql + condition.getRefundFilter(AccountType.CONSUMER) + groupBy + " order by sum(e.refundPrice) desc");

        for (String param : condition.getParamMap1().keySet()) {
            query.setParameter(param, condition.getParamMap1().get(param));
        }

        List<ChannelGoodsReport> refundList = query.getResultList();

        Map<String, ChannelGoodsReport> map = new HashMap<>();

        //merge
        for (ChannelGoodsReport paidItem : paidResultList) {
            map.put(getConsumerReportKey(paidItem), paidItem);
        }

        for (ChannelGoodsReport refundItem : refundList) {
            ChannelGoodsReport item = map.get(getConsumerReportKey(refundItem));
            if (item == null) {
                Goods goods = Goods.findById(refundItem.goods.id);
                refundItem.originalPrice = goods.originalPrice;
                refundItem.netSalesAmount = BigDecimal.ZERO.subtract(refundItem.refundAmount);
                map.put(getConsumerReportKey(refundItem), refundItem);
            } else {
                item.refundAmount = refundItem.refundAmount;
                item.netSalesAmount = item.totalAmount.subtract(item.refundAmount);
            }
        }

        //merge from resaler if commissionRatio
        for (ChannelGoodsReport resalerItem : paidResalerResultList) {
            ChannelGoodsReport item = map.get(getConsumerReportKey(resalerItem));
            if (item == null) {
                map.put(getConsumerReportKey(resalerItem), resalerItem);
            } else {
                item.profit = item.profit == null ? BigDecimal.ZERO : item.profit.subtract(resalerItem.totalAmount == null ? BigDecimal.ZERO : resalerItem.totalAmount
                        .subtract(resalerItem.totalCost == null ? BigDecimal.ZERO : resalerItem.totalCost))
                        .add(resalerItem.profit == null ? BigDecimal.ZERO : resalerItem.totalCost);
            }
        }

        List resultList = new ArrayList();
        for (String key : map.keySet()) {
            resultList.add(map.get(key));
        }

        return resultList;
    }

    /**
     * 取得净销售的总计
     *
     * @param resultList
     * @return
     */
    public static ChannelGoodsReport getNetSummary(List<ChannelGoodsReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new ChannelGoodsReport(null, 0l, BigDecimal.ZERO);
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal netSalesAmount = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;
        for (ChannelGoodsReport item : resultList) {
            totalAmount = totalAmount.add(item.totalAmount == null ? BigDecimal.ZERO : item.totalAmount);
            netSalesAmount = netSalesAmount.add(item.netSalesAmount == null ? BigDecimal.ZERO : item.netSalesAmount);
            refundAmount = refundAmount.add(item.refundAmount == null ? BigDecimal.ZERO : item.refundAmount);
        }
        return new ChannelGoodsReport(totalAmount, refundAmount, netSalesAmount);
    }

    public static String getReportKey(ChannelGoodsReport refoundItem) {
        if (refoundItem.order == null) {
            System.out.println("null order.goods.id>>>>" + refoundItem.goods.id);
            return String.valueOf(refoundItem.goods.id);
        } else {
            System.out.println("order.userId>>>>" + refoundItem.order.userId);
            System.out.println("order.goods.id>>>>" + refoundItem.goods.id);
            return String.valueOf(refoundItem.order.userId) + String.valueOf(refoundItem.goods.id);
        }
    }

    public static String getConsumerReportKey(ChannelGoodsReport refoundItem) {
        return String.valueOf(refoundItem.goods.id);
    }

}
