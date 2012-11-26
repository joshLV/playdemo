package models;

import models.accounts.AccountType;
import models.order.ECoupon;
import models.order.ECouponStatus;
import models.order.Order;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:51
 */
@Entity
@Table(name = "resale_sales_report")
public class ResaleSalesReport extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    public Order order;
    public String loginName;
    public String userName;
    /**
     * 售出券数
     */
    public long buyNumber;

    /**
     * 退款券数
     */
    public long refundNumber;
    /**
     * 消费券数
     */
    public long consumedNumber;

    /**
     * 售出金额
     */
    public BigDecimal salePrice;

    /**
     * 退款金额
     */
    public BigDecimal refundPrice;

    /**
     * 消费金额
     */
    public BigDecimal consumedPrice;

    /**
     * 应收款金额
     */
    public BigDecimal shouldGetPrice;
    /**
     * 已收款金额
     */
    public BigDecimal haveGetPrice;

    public Long totalNumber;
    public BigDecimal amount;
    public BigDecimal totalRefundPrice;

    public ResaleSalesReport(Order order, BigDecimal salePrice, Long buyNumber) {
        this.order = order;
        if (order != null) {
            if (order.userType == AccountType.CONSUMER) {
                this.loginName = "一百券";
            } else {
                this.loginName = order.getResaler().loginName;
                this.userName = order.getResaler().userName;
            }
        }

        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
    }

    public ResaleSalesReport(BigDecimal salePrice, Long buyNumber, BigDecimal refundPrice, Long refundCount, BigDecimal consumedPrice, Long consumedCount) {
        this.userName = "一百券";
        this.salePrice = salePrice;
        this.buyNumber = buyNumber;
        this.refundPrice = refundPrice;
        this.refundNumber = refundCount;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedCount;

    }

    public ResaleSalesReport(long totalNumber, BigDecimal amount, BigDecimal totalRefundPrice, Long refundNumber,
                             BigDecimal consumedPrice, Long consumedNumber, BigDecimal shouldGetPrice, BigDecimal haveGetPrice) {
        this.totalNumber = totalNumber;
        this.amount = amount;
        this.totalRefundPrice = totalRefundPrice;
        this.refundNumber = refundNumber;
        this.consumedPrice = consumedPrice;
        this.consumedNumber = consumedNumber;
        this.shouldGetPrice = shouldGetPrice;
        this.haveGetPrice = haveGetPrice;
    }

    /**
     * 分销商报表统计
     *
     * @param condition
     * @return
     */
    public static List<ResaleSalesReport> query(
            ResaleSalesReportCondition condition) {
        String sql = "select new models.ResaleSalesReport(e.order,sum(e.salePrice),count(e.orderItems.buyNumber)) from ECoupon e ";
        String groupBy = " group by e.order.userId";

        Query query = JPA.em()
                .createQuery(sql + condition.getFilter(AccountType.RESALER) + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ResaleSalesReport> resultList = query.getResultList();
        List<Order> newList = new ArrayList<Order>();
        for (ResaleSalesReport item : resultList) {
            long consumedCount = 0l;
            long refundCount = 0l;
            BigDecimal consumedPrice = BigDecimal.ZERO;
            BigDecimal refundPrice = BigDecimal.ZERO;
            newList = Order.find("userId=? and userType=?", item.order.userId, AccountType.RESALER).fetch();
            for (Order order : newList) {
                List<ECoupon> eCoupons = ECoupon.find("order=?", order).fetch();
                for (ECoupon coupon : eCoupons) {
                    if (coupon.status == ECouponStatus.CONSUMED) {
                        consumedCount++;
                        consumedPrice = consumedPrice.add(coupon.salePrice == null ? BigDecimal.ZERO : coupon.salePrice);
                    }

                    if (coupon.status == ECouponStatus.REFUND) {
                        refundCount++;
                        refundPrice = refundPrice.add(coupon.refundPrice == null ? BigDecimal.ZERO : coupon.refundPrice);
                    }
                }
            }
            item.consumedPrice = consumedPrice;
            item.consumedNumber = consumedCount;
            item.refundNumber = refundCount;
            item.refundPrice = refundPrice;
        }

        return resultList;
    }

    /**
     * 消费者和分销商合计
     *
     * @param resultList
     * @return
     */
    public static ResaleSalesReport summary(List<ResaleSalesReport> resultList) {
        if (resultList == null || resultList.size() == 0) {
            return new ResaleSalesReport(0l, BigDecimal.ZERO, BigDecimal.ZERO, 0l, BigDecimal.ZERO, 0l, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        long refundCount = 0l;
        long consumedCount = 0l;
        BigDecimal consumedPrice = BigDecimal.ZERO;
        long buyCount = 0l;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal refundPrice = BigDecimal.ZERO;
        BigDecimal totRefundPrice = BigDecimal.ZERO;
        BigDecimal shouldGetPrice = BigDecimal.ZERO;
        BigDecimal haveGetPrice = BigDecimal.ZERO;
        for (ResaleSalesReport item : resultList) {

            buyCount += item.buyNumber;
            amount = amount.add(item.salePrice == null ? BigDecimal.ZERO : item.salePrice);
            totRefundPrice = item.refundPrice == null ? BigDecimal.ZERO : item.refundPrice;
            refundPrice = refundPrice.add(totRefundPrice);
            refundCount += item.refundNumber;
            consumedCount += item.consumedNumber;
            consumedPrice = consumedPrice.add(item.consumedPrice);
            shouldGetPrice = amount.subtract(refundPrice);
            haveGetPrice = BigDecimal.ZERO;
        }
        return new ResaleSalesReport(buyCount, amount, refundPrice, refundCount, consumedPrice, consumedCount, shouldGetPrice, haveGetPrice);
    }

    /**
     * 消费者报表统计
     *
     * @param condition
     * @return
     */
    public static List<ResaleSalesReport> queryConsumer(ResaleSalesReportCondition condition) {
        String sql = "select new models.ResaleSalesReport(e.order,sum(e.salePrice),count(e.orderItems.buyNumber)) from ECoupon e ";
        String groupBy = " group by e.order";

        Query query = JPA.em()
                .createQuery(sql + condition.getFilter(AccountType.CONSUMER) + groupBy + " order by sum(e.salePrice) desc");

        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<ResaleSalesReport> resultList = query.getResultList();
        long refundCount = 0l;
        long consumedCount = 0l;
        BigDecimal consumedPrice = BigDecimal.ZERO;

        long buyCount = 0l;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal refundPrice = BigDecimal.ZERO;
        List<ResaleSalesReport> newList = new ArrayList<ResaleSalesReport>();
        for (ResaleSalesReport item : resultList) {
            for (ECoupon coupon : item.order.eCoupons) {
                if (coupon.status == ECouponStatus.REFUND) {
                    refundCount++;
                    refundPrice = refundPrice.add(coupon.refundPrice == null ? BigDecimal.ZERO : coupon.refundPrice);
                } else if (coupon.status == ECouponStatus.CONSUMED) {
                    consumedCount++;
                    consumedPrice = consumedPrice.add(coupon.salePrice == null ? BigDecimal.ZERO : coupon.salePrice);
                }
            }

            buyCount += item.buyNumber;
            amount = amount.add(item.salePrice);
        }

        newList.add(new ResaleSalesReport(amount, buyCount, refundPrice, refundCount, consumedPrice, consumedCount));

        return newList;
    }

}
