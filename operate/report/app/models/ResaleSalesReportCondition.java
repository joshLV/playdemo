package models;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import play.data.binding.types.DateBinder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:52
 */
public class ResaleSalesReportCondition {
    public Date beginAt = DateUtil.getBeginOfDay();
    public Date endAt = DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    public AccountType accountType;
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilterPaidAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder("and r.order.status='PAID' " +
                "and r.order.userType = :userType " +
                "and r.goods.isLottery=false");
        paramMap.put("userType", type);

        if (beginAt != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
        }


        System.out.println("condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }

    public String getFilterRealSendAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" where r.order.status='SENT' " +
                "and r.order.userType = :userType " +
                "and r.goods.isLottery=false and r.goods.materialType=models.sales.MaterialType.REAL");

        paramMap.put("userType", type);

        if (beginAt != null) {
            condBuilder.append(" and r.sendAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.sendAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
        }


        System.out.println("sendAt condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }


    public String getFilterConsumedAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID' and r.order.userType = :userType and r.goods.isLottery=false and e.status = models.order.ECouponStatus.CONSUMED");

        paramMap.put("userType", type);

        if (beginAt != null) {
            condBuilder.append(" and e.consumedAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.consumedAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
        }


        System.out.println("condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }

    public String getFilterRefundAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID' and e.order.userType = :userType and e.goods.isLottery=false and e.status = models.order.ECouponStatus.REFUND");

        paramMap.put("userType", type);

        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
        }


        System.out.println("condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }

    public String getFilterRealRefundAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" where r.order.status='SENT' and r.order.userType = :userType and r.goods.isLottery=false");

        paramMap.put("userType", type);

        if (beginAt != null) {
            condBuilder.append(" and r.order.refundAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.refundAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
        }


        System.out.println("condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }


    public Map<String, Object> getParamMap() {
        return paramMap;
    }

}
