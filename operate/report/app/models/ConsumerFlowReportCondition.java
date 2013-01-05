package models;

import models.accounts.AccountType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-12-28
 * Time: 下午5:30
 * To change this template use File | Settings | File Templates.
 */
public class ConsumerFlowReportCondition {
    public Date beginAt = com.uhuila.common.util.DateUtil.getBeginOfDay();
    public Date endAt = com.uhuila.common.util.DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    private Map<String, Object> paramMap = new HashMap<>();
    private Map<String, Object> paramMap1 = new HashMap<>();


    public String getFilterPaidAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder("and (r.order.status='PAID' or r.order.status='SENT') " +
                " and r.order.userType = :userType " +
                " and r.goods.isLottery=false and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED "
        );

        paramMap.put("userType", type);

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

    public String getFilterRealSendAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder("(r.order.status='PAID' or r.order.status='SENT')  " +
                "and r.goods.isLottery=false and r.goods.materialType=models.sales.MaterialType.REAL" +
                " and r.order.userType = :userType " +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order.deliveryType=models.order.DeliveryType.LOGISTICS");

        paramMap.put("userType", type);

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


    public String getFilterConsumedAt() {
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID' and r.goods.isLottery=false and e.status = models.order.ECouponStatus.CONSUMED" +
                " and  r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");

        if (beginAt != null) {
            condBuilder.append(" and e.consumedAt >= :createdAtBegin");
            paramMap1.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.consumedAt < :createdAtEnd");
            paramMap1.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();
    }

    public String getFilterRefundAt() {
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID' and e.goods.isLottery=false and e.status = models.order.ECouponStatus.REFUND" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");

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

    public String getFilterRealRefundAt() {
        StringBuilder condBuilder = new StringBuilder(" where r.order.status='SENT' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");

        if (beginAt != null) {
            condBuilder.append(" and r.order.refundAt >= :createdAtBegin");
            paramMap1.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.refundAt < :createdAtEnd");
            paramMap1.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
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
