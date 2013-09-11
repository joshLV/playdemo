package models;

import models.order.ECouponStatus;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-12-25
 * Time: 上午10:14
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
    public Long supplierId = 0l;

    //排序字段
    public String desc;
    public int orderByIndex;
    public String orderByType;

    Map<String, BigDecimal> comparedMap = new HashMap<>();

    public String getFilter() {
//               +  " and r.order.userType = models.accounts.AccountType.RESALER "
        StringBuilder condBuilder = new StringBuilder(" (r.order.status='PAID' or r.order.status='SENT' " +
                "or r.order.status = 'PREPARED' or r.order.status='UPLOADED') and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED ");
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
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }
        return condBuilder.toString();
    }

    public String getFilterCheatedOrderResaler() {
        StringBuilder condBuilder = new StringBuilder("  r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and e.isCheatedOrder = true  and r.order=o and o.userId=b.id" +
                " ");
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
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }
        return condBuilder.toString();
    }

    public String getFilterRefundResaler() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                "  and r.order=o and o.userId=b.id " +
                " ");
        paramMap1.put("status", ECouponStatus.REFUND);
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
        if (supplierId != 0) {
            condBuilder.append(" and e.goods.supplierId = :supplierId");
            paramMap1.put("supplierId", supplierId);
        }
        return condBuilder.toString();
    }

    public String getFilterConsumedAt() {
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID' " +
                " and r.goods.isLottery=false and e.status = models.order.ECouponStatus.CONSUMED" +
                " and  r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " ");

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
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }

    public String getFilterCheatedOrder() {
        StringBuilder condBuilder = new StringBuilder(" r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and e.isCheatedOrder = true ");
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
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }
        return condBuilder.toString();
    }


    public String getResalerFilter() {
        StringBuilder condBuilder = new StringBuilder("  " +
                "  (r.order.status='PAID' or r.order.status='SENT')" +
                " and r.goods.isLottery=false and r.order=o and o.userId=b.id" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED  ");
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
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }
        System.out.println(condBuilder.toString() + "《=========condBuilder.toString():");
        return condBuilder.toString();
    }

    public String getRefundFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED ");
        paramMap1.put("status", ECouponStatus.REFUND);
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
        if (supplierId != 0) {
            condBuilder.append(" and e.goods.supplierId = :supplierId");
            paramMap1.put("supplierId", supplierId);
        }
        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public Map<String, Object> getParamMap1() {
        return paramMap1;
    }

    public void setDescFields() {
        // DESC 的值表示升降序，含11位，代表11个排序字段， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 000002000000
        String orderBy = "";
        if (desc == null) {
            desc = "020000000";
        }
        // 获取最新的desc值
        String[] descs = desc.split(",");
        desc = descs[descs.length - 1].trim();
        if (isValidDesc(desc)) {
            int index = 0;
            // 定位排序属性
            for (int i = 0; i < desc.length(); i++) {
                if (desc.charAt(i) != '0') {
                    index = i;
                    orderByIndex = i;
                    break;
                }
            }
            if (desc.charAt(index) == '1') {
                orderByType = "1";
            } else {
                orderByType = "2";
            }
        } else {
            orderBy = "52";
        }
    }

    public static boolean isValidDesc(String desc) {
        if (desc.length() != 9) {
            return false;
        }
        int countZero = 0;
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) == '0') {
                countZero++;
            }
        }
        if (countZero != 8) {
            return false;
        }
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) != '0' && desc.charAt(i) != '1' && desc.charAt(i) != '2') {
                return false;
            }
        }
        return true;
    }
}
