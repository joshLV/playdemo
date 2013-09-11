package models;

import models.order.ECouponStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 渠道销售汇总日报表查询条件
 * <p/>
 * User: wangjia
 * Date: 13-4-11
 * Time: 下午4:07
 */
public class ChannelSalesDailyReportCondition implements Serializable {
    public Date beginAt = com.uhuila.common.util.DateUtil.getBeforeDate(new Date(), 8);
    public Date endAt = new Date();
    private Map<String, Object> paramMap = new HashMap<>();
    private Map<String, Object> paramMap1 = new HashMap<>();

    public String getPaidAtFilter() {
        StringBuilder condBuilder = new StringBuilder(" and (r.order.status='PAID' or r.order.status='SENT' " +
                "or r.order.status = 'PREPARED' or r.order.status='UPLOADED') and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED ");
        if (beginAt != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();
    }

    public String getRefundAtFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED ");
        paramMap1.put("status", ECouponStatus.REFUND);
        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :createdAtBegin");
            paramMap1.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt < :createdAtEnd");
            paramMap1.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        return condBuilder.toString();
    }

    public String getCheatedOrderFilter() {
        StringBuilder condBuilder = new StringBuilder(" r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and e.isCheatedOrder = true ");
        if (beginAt != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        return condBuilder.toString();
    }


    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public Map<String, Object> getParamMap1() {
        return paramMap1;
    }


}
