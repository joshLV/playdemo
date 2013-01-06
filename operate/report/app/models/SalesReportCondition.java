package models;

import com.uhuila.common.util.DateUtil;
import models.order.ECouponStatus;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 销售报表查询条件
 * <p/>
 * User: wangjia
 * Date: 12-12-11
 * Time: 下午4:07
 */
public class SalesReportCondition implements Serializable {
    public Date beginAt = com.uhuila.common.util.DateUtil.getBeginOfDay();
    public Date endAt = com.uhuila.common.util.DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    public String shortName;
    public String code;
    public String userName;
    public String jobNumber;
    private Map<String, Object> paramMap = new HashMap<>();
    private Map<String, Object> paramMap1 = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder(" where (r.order.status='PAID' or r.order.status='SENT') and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code = :code");
            paramMap.put("code", code.trim() + "%");
        }

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
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID' " +
                " and r.goods.isLottery=false and e.status = models.order.ECouponStatus.CONSUMED" +
                " and  r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");

        if (beginAt != null) {
            condBuilder.append(" and e.consumedAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.consumedAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();
    }


    public String getResalerFilter() {
        StringBuilder condBuilder = new StringBuilder(" where r.order.userType=models.accounts.AccountType.RESALER " +
                " and (r.order.status='PAID' or r.order.status='SENT')" +
                " and r.goods.isLottery=false and r.order=o and o.userId=b.id" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED ");
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code = :code");
            paramMap.put("code", code.trim() + "%");
        }
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


    public String getRefundFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");
        paramMap1.put("status", ECouponStatus.REFUND);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and e.goods.shortName like :shortName");
            paramMap1.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and e.goods.code = :code");
            paramMap1.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap1.put("refundAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap1.put("refundAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public Map<String, Object> getParamMap1() {
        return paramMap1;
    }

    public String getFilterOfPeopleEffect() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId =s.id and s.deleted=0 and s.salesId=o.id and o.deleted=0  and (r.order.status='PAID' or r.order.status='SENT') and r.goods.isLottery=false");

        if (StringUtils.isNotBlank(userName)) {
            condBuilder.append(" and o.userName like :shortName");
            paramMap.put("shortName", "%" + userName + "%");
        }
        if (StringUtils.isNotBlank(jobNumber)) {
            condBuilder.append(" and o.jobNumber= :jobNumber");
            paramMap.put("jobNumber", jobNumber);
        }

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

    public String getResalerFilterOfPeopleEffect() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId =s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and r.order.userType=models.accounts.AccountType.RESALER " +
                " and (r.order.status='PAID' or r.order.status='SENT')" +
                " and r.goods.isLottery=false and r.order=o and o.userId=b.id");
        if (StringUtils.isNotBlank(userName)) {
            condBuilder.append(" and ou.userName like :shortName");
            paramMap.put("shortName", "%" + userName + "%");
        }
        if (StringUtils.isNotBlank(jobNumber)) {
            condBuilder.append(" and ou.jobNumber= :jobNumber");
            paramMap.put("jobNumber", jobNumber);
        }
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

    public String getRefundFilterOfPeopleEffect(ECouponStatus status) {
        paramMap1 = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder(" where e.goods.supplierId=s.id and s.deleted=0 and s.salesId=o.id and o.deleted=0 and e.status=:status and e.goods.isLottery=false");
        paramMap1.put("status", status);

        if (StringUtils.isNotBlank(userName)) {
            condBuilder.append(" and o.userName like :userName");
            paramMap1.put("userName", "%" + userName + "%");
        }
        if (StringUtils.isNotBlank(jobNumber)) {
            condBuilder.append(" and o.jobNumber=:jobNumber");
            paramMap1.put("jobNumber", jobNumber);
        }
        if (status == ECouponStatus.REFUND) {
            if (beginAt != null) {
                condBuilder.append(" and e.refundAt >= :refundAtBegin");
                paramMap1.put("refundAtBegin", beginAt);
            }
            if (endAt != null) {
                condBuilder.append(" and e.refundAt <= :refundAtEnd");
                paramMap1.put("refundAtEnd", DateUtil.getEndOfDay(endAt));
            }

        } else if (status == ECouponStatus.CONSUMED) {
            if (beginAt != null) {
                condBuilder.append(" and e.consumedAt>= :consumedAtBegin");
                paramMap1.put("consumedAtBegin", beginAt);
            }
            if (endAt != null) {
                condBuilder.append(" and e.consumedAt <= :consumedAtEnd");
                paramMap1.put("consumedAtEnd", DateUtil.getEndOfDay(endAt));
            }

        }
        return condBuilder.toString();
    }

}
