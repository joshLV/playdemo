package models;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 渠道大类查询条件
 * <p/>
 * User: wangjia
 * Date: 12-12-20
 * Time: 下午5:57
 */
public class PeopleEffectCategoryReportCondition implements Serializable {
    public Date beginAt = com.uhuila.common.util.DateUtil.getBeginOfDay();
    public Date endAt = com.uhuila.common.util.DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    public String userName;
    public String jobNumber;
    public AccountType accountType;
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilterPaidAt() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId = s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and r.order.status='PAID' " +
                "and r.goods.isLottery=false and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED"
        );

        if (beginAt != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();
    }

    public String getFilterRealSendAt() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId =s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and (r.order.status='PAID' or r.order.status='SENT')  " +
                "and r.goods.isLottery=false and r.goods.materialType=models.sales.MaterialType.REAL" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order.deliveryType=models.order.DeliveryType.LOGISTICS");


        if (beginAt != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();
    }

    public String getFilterConsumedAt() {
        StringBuilder condBuilder = new StringBuilder(" where e.goods.supplierId =s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and e.goods.isLottery=false and e.status = models.order.ECouponStatus.CONSUMED");


        if (beginAt != null) {
            condBuilder.append(" and e.consumedAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.consumedAt < :createdAtEnd");
            paramMap.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();
    }

    public String getFilterRefundAt() {
        StringBuilder condBuilder = new StringBuilder(" where e.goods.supplierId =s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and e.goods.isLottery=false and e.status = models.order.ECouponStatus.REFUND");

        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt < :createdAtEnd");
            paramMap.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();
    }

    public String getFilterRealRefundAt() {
        StringBuilder condBuilder = new StringBuilder(" where r.order.status='SENT' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");

        if (beginAt != null) {
            condBuilder.append(" and r.order.refundAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.refundAt < :createdAtEnd");
            paramMap.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();
    }


    public Map<String, Object> getParamMap() {
        return paramMap;
    }


}
