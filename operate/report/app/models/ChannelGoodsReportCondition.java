package models;

import models.accounts.AccountType;
import models.order.ECouponStatus;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-12-25
 * Time: 上午10:14
 * To change this template use File | Settings | File Templates.
 */
public class ChannelGoodsReportCondition {
    public Date beginAt = com.uhuila.common.util.DateUtil.getBeginOfDay();
    public Date endAt = com.uhuila.common.util.DateUtil.getEndOfDay(new Date());
    public String shortName;
    public String code;
    public String interval = "-1d";
    private Map<String, Object> paramMap = new HashMap<>();
    private Map<String, Object> paramMap1 = new HashMap<>();
    public Boolean hasSeeReportProfitRight;
    public Long operatorId;

    public String getFilter(AccountType type) {
//               +  " and r.order.userType = models.accounts.AccountType.RESALER "
        StringBuilder condBuilder = new StringBuilder(" (r.order.status='PAID' or r.order.status='SENT') and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and r.order.userType = :userType ");
        paramMap.put("userType", type);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code like :code");
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

        return condBuilder.toString();
    }

    public String getFilterCheatedOrderResaler(AccountType type) {
        StringBuilder condBuilder = new StringBuilder("  r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and e.isCheatedOrder = true and r.order.userType=models.accounts.AccountType.RESALER and r.order=o and o.userId=b.id" +
                " and r.order.userType = :userType ");
        paramMap.put("userType", type);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }

        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code like :code");
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

        return condBuilder.toString();
    }

    public String getFilterRefundResaler(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order.userType=models.accounts.AccountType.RESALER and r.order=o and o.userId=b.id " +
                " and r.order.userType = :userType ");
        paramMap1.put("status", ECouponStatus.REFUND);
        paramMap1.put("userType", type);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and e.goods.shortName like :shortName");
            paramMap1.put("shortName", "%" + shortName + "%");
        }

        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and e.goods.code like :code");
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
        if (hasSeeReportProfitRight != null && !hasSeeReportProfitRight) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap1.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 5 =:supplierIds");
                paramMap1.put("supplierIds", 6);
            }
        }

        return condBuilder.toString();
    }

    public String getFilterConsumedAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID' " +
                " and r.goods.isLottery=false and e.status = models.order.ECouponStatus.CONSUMED" +
                " and  r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order.userType = :userType ");
        paramMap.put("userType", type);

        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code like :code");
            paramMap.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and e.consumedAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.consumedAt < :createdAtEnd");
            paramMap.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
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

        return condBuilder.toString();
    }

    public String getFilterCheatedOrder(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order.userType = :userType " +
                " and e.isCheatedOrder = true ");
        paramMap.put("userType", type);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code like :code");
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

        return condBuilder.toString();
    }


    public String getResalerFilter(AccountType type) {
        StringBuilder condBuilder = new StringBuilder("  r.order.userType=models.accounts.AccountType.RESALER " +
                " and (r.order.status='PAID' or r.order.status='SENT')" +
                " and r.goods.isLottery=false and r.order=o and o.userId=b.id" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and r.order.userType = :userType ");
        paramMap.put("userType", type);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code like :code");
            paramMap1.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
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
        return condBuilder.toString();
    }

    public String getRefundFilter(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" where e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and e.order.userType = :userType ");
        paramMap1.put("status", ECouponStatus.REFUND);
        paramMap1.put("userType", type);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and e.goods.shortName like :shortName");
            paramMap1.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and e.goods.code like :code");
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
        if (hasSeeReportProfitRight != null && !hasSeeReportProfitRight) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap1.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 5 =:supplierIds");
                paramMap1.put("supplierIds", 6);
            }
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
