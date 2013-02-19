package models;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.order.ECouponStatus;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:52
 */
public class OperateResaleSalesReportCondition {
    public Date beginAt = DateUtil.getBeginOfDay();
    public Date endAt = DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    public AccountType accountType;
    private Map<String, Object> paramMap = new HashMap<>();
    public Boolean hasSeeReportProfitRight;
    public Long operatorId;
    public String goodsCode;

    public String getFilterPaidAt(AccountType type) {
        paramMap = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder("and (r.order.status='PAID' or r.order.status='SENT') " +
                "and r.order.userType = :userType " +
                "and r.goods.isLottery=false and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED"
        );


        paramMap.put("userType", type);

        if (beginAt != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
        }
        setCommonSqlCondition(condBuilder);

        return condBuilder.toString();
    }

    private void setCommonSqlCondition(StringBuilder condBuilder) {
        if (hasSeeReportProfitRight != null && !hasSeeReportProfitRight) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and r.goods.supplierId in (:supplierIds)");
                paramMap.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 5 =:supplierIds");
                paramMap.put("supplierIds", 6);
            }
        }
        if (StringUtils.isNotBlank(goodsCode)) {
            condBuilder.append(" and r.goods.code like :goodsCode");
            paramMap.put("goodsCode", goodsCode.trim() + "%");
        }
    }

    public String getFilterRealSendAt(AccountType type) {
        paramMap = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder("(r.order.status='PAID' or r.order.status='SENT')  " +
                "and r.order.userType = :userType " +
                "and r.goods.isLottery=false and r.goods.materialType=models.sales.MaterialType.REAL" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order.deliveryType=models.order.DeliveryType.LOGISTICS");

        paramMap.put("userType", type);

        if (beginAt != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
        }
        setCommonSqlCondition(condBuilder);

        return condBuilder.toString();
    }

    public String getFilterOfECoupon(AccountType type, ECouponStatus status) {
        paramMap = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID' and r.order.userType = :userType and r.goods.isLottery=false and e.status = :status" +
                " and  r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");

        paramMap.put("userType", type);
        paramMap.put("status", status);

        if (status == ECouponStatus.CONSUMED) {
            if (beginAt != null) {
                condBuilder.append(" and e.consumedAt >= :createdAtBegin");
                paramMap.put("createdAtBegin", beginAt);
            }
            if (endAt != null) {
                condBuilder.append(" and e.consumedAt < :createdAtEnd");
                paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
            }
        } else if (status == ECouponStatus.REFUND) {
            if (beginAt != null) {
                condBuilder.append(" and e.consumedAt >= :createdAtBegin");
                paramMap.put("createdAtBegin", beginAt);
            }
            if (endAt != null) {
                condBuilder.append(" and e.consumedAt < :createdAtEnd");
                paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
            }
        }

        setCommonSqlCondition(condBuilder);
        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public String getFilterVirtualVerfiyAt(AccountType type) {
        paramMap = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder(" where e.order.status='PAID' and e.order.userType = :userType and e.goods.isLottery=false and e.status = :status" +
                " and e.virtualVerify =true and (e.goods.noRefund = true or e.isCheatedOrder = true) and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");
        paramMap.put("userType", type);
        paramMap.put("status", ECouponStatus.UNCONSUMED);
        if (beginAt != null) {
            condBuilder.append(" and e.virtualVerifyAt>= :beginAt");
            paramMap.put("beginAt", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.virtualVerifyAt < :endAt");
            paramMap.put("endAt", DateUtil.getEndOfDay(endAt));
        }
        setCommonSqlCondition(condBuilder);

        return condBuilder.toString();

    }
}
