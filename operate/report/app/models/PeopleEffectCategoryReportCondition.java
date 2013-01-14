package models;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.order.ECouponStatus;
import operate.rbac.ContextedPermission;
import org.apache.commons.lang.StringUtils;

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
    public Long salesId;
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilterPaidAt() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId = s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and r.order.status='PAID' " +
                "and r.goods.isLottery=false and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED"
        );

        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
            paramMap.put("salesId", salesId);
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

    public String getFilterRealSendAt() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId =s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and (r.order.status='PAID' or r.order.status='SENT')  " +
                "and r.goods.isLottery=false and r.goods.materialType=models.sales.MaterialType.REAL" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order.deliveryType=models.order.DeliveryType.LOGISTICS");

        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
            paramMap.put("salesId", salesId);
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

    public String getFilterConsumedAt() {
        StringBuilder condBuilder = new StringBuilder(" where e.goods.supplierId =s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and e.goods.isLottery=false and e.status = models.order.ECouponStatus.CONSUMED");

        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
            paramMap.put("salesId", salesId);
        }

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
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.goods.supplierId =s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and e.goods.isLottery=false and e.status = models.order.ECouponStatus.REFUND");

        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
            paramMap.put("salesId", salesId);
        }
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

        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
            paramMap.put("salesId", salesId);
        }
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


    public String getFilterOfPeopleEffect() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId =s.id and s.deleted=0 and s.salesId=o.id and o.deleted=0  and (r.order.status='PAID' or r.order.status='SENT') and r.goods.isLottery=false");
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and o.id =:salesId");
            paramMap.put("salesId", salesId);
        }
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
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and o.id =:salesId");
            paramMap.put("salesId", salesId);
        }
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

    private Map<String, Object> paramMap1 = new HashMap<>();

    public String getRefundFilterOfPeopleEffect(ECouponStatus status) {
        paramMap1 = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and r.goods.supplierId =s.id and e.goods.supplierId=s.id and s.deleted=0 and s.salesId=o.id and o.deleted=0 and e.status=:status and e.goods.isLottery=false");
        paramMap1.put("status", status);
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and o.id =:salesId");
            paramMap1.put("salesId", salesId);
        }
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

    public String getFilterCheatedOrderOfPeopleEffect() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.goods.supplierId=s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and e.isCheatedOrder = true ");
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
            paramMap.put("salesId", salesId);
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

    public String getFilterCheatedOrderResalerOfPeopleEffect() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.goods.supplierId=s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and e.isCheatedOrder = true and r.order.userType=models.accounts.AccountType.RESALER and r.order=o and o.userId=b.id ");
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and o.id =:salesId");
            paramMap.put("salesId", salesId);
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

    public String getFilterRefundResalerOfPeopleEffect() {
        paramMap1 = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.goods.supplierId=s.id and s.deleted=0 and s.salesId=ou.id and ou.deleted=0 and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order.userType=models.accounts.AccountType.RESALER and r.order=o and o.userId=b.id ");
        paramMap1.put("status", ECouponStatus.REFUND);
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
            paramMap1.put("salesId", salesId);
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

}
