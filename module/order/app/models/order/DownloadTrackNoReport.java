package models.order;

import models.accounts.AccountType;
import models.resale.Resaler;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 渠道下载运单报表
 * <p/>
 * User: wangjia
 * Date: 13-3-14
 * Time: 上午11:44
 */
public class DownloadTrackNoReport {

    public OrderItems orderItems;
    public String outerGoodsNo;
    public String goodsName;

    public String partnerLoginName;

    /*
       已发货数
    */
    public Long sentCount;

    /**
     * 总金额
     */
    public BigDecimal totalAmount;


    public DownloadTrackNoReport(OrderItems orderItems, Long sentCount, BigDecimal totalAmount) {
        this.orderItems = orderItems;
        this.outerGoodsNo = orderItems.outerGoodsNo;
        Resaler resaler = Resaler.findById(orderItems.order.userId);
        this.partnerLoginName = resaler.loginName;
//        if (orderItems != null && StringUtils.isNotBlank(orderItems.outerGoodsNo)) {
//
//        }

        this.sentCount = sentCount;
        this.totalAmount = totalAmount;
    }

    /**
     * 取得按外部商品ID统计的渠道下载运单
     *
     * @param condition
     * @return
     */
    public static List<DownloadTrackNoReport> query(DownloadTrackNoCondition condition) {
        String sql = "select new models.order.DownloadTrackNoReport(oi,count(distinct oi.shippingInfo.id),sum(oi.salePrice*oi.buyNumber))" +
                " from OrderItems oi";
        String groupBy = " group by oi.outerGoodsNo";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy);


        for (String param : condition.getParams().keySet()) {
            query.setParameter(param, condition.getParams().get(param));
        }

        List<DownloadTrackNoReport> resultList = query.getResultList();
        return resultList;
    }

    public static List<OrderItems> queryOrderItems(OuterOrderPartner partner, Date paidBeginAt, Date paidEndAt, Date sentBeginAt, Date sentEndAt, String outerGoodsNo) {
        StringBuilder sql = new StringBuilder("shippingInfo.expressNumber is not null and order.userType=? and order.userId=?");
        List<Object> params = new ArrayList();
        params.add(AccountType.RESALER);
        params.add(Resaler.findOneByLoginName(partner.partnerLoginName()).id);
        if (StringUtils.isNotBlank(outerGoodsNo)) {
            sql.append(" and outerGoodsNo= ?");
            params.add(outerGoodsNo);
        }

        if (paidBeginAt != null) {
            sql.append(" and shippingInfo.paidAt>= ?");
            params.add(paidBeginAt);
        }
        if (paidEndAt != null) {
            sql.append(" and shippingInfo.paidAt<= ?");
            params.add(com.uhuila.common.util.DateUtil.getEndOfDay(paidEndAt));
        }

        if (sentBeginAt != null) {
            sql.append(" and sendAt >= ?)");
            params.add(sentBeginAt);
        }
        if (paidEndAt != null) {
            sql.append(" and sendAt <= ?)");
            params.add(com.uhuila.common.util.DateUtil.getEndOfDay(sentEndAt));
        }

        return OrderItems.find(sql.toString(), params.toArray()).fetch();

    }
}
