package models;

import models.order.ECouponStatus;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-12-28
 * Time: 上午11:54
 * To change this template use File | Settings | File Templates.
 */
public class CategorySalesReportCondition {
    public Date beginAt = com.uhuila.common.util.DateUtil.getBeginOfDay();
    public Date endAt = com.uhuila.common.util.DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    public String shortName;
    public String code;
    public String userName;
    public String jobNumber;
    private Map<String, Object> paramMap = new HashMap<>();
    private Map<String, Object> paramMap1 = new HashMap<>();
    public Boolean hasSeeReportProfitRight;
    public Long operatorId;
    //排序字段
    public String desc;
    public int orderByIndex;
    public String orderByType;


    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId = s and (r.order.status='PAID' or r.order.status='SENT') and r.goods.isLottery=false" +
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


    public String getFilterCheatedOrder() {
        StringBuilder condBuilder = new StringBuilder(" r.goods.supplierId = s and r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and e.isCheatedOrder = true ");
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

    public String getFilterCheatedOrderResaler() {
        StringBuilder condBuilder = new StringBuilder(" r.goods.supplierId = s and r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and e.isCheatedOrder = true and r.order=o and o.userId=b.id and r.order.userType=models.accounts.AccountType.RESALER ");
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


    public String getResalerFilter() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId = s and r.order.userType=models.accounts.AccountType.RESALER " +
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

    public String getFilterConsumedAt() {
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID' " +
                " and r.goods.isLottery=false and e.status = models.order.ECouponStatus.CONSUMED" +
                " and  r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code = :code");
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

    public String getRefundFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems.goods.supplierId = s and e.status=:status and e.goods.isLottery=false" +
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

    public String getFilterRefundResaler() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.orderItems.goods.supplierId = s and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order=o and o.userId=b.id and r.order.userType=models.accounts.AccountType.RESALER ");
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

    public void setDescFields() {
        // DESC 的值表示升降序，含11位，代表11个排序字段， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 000002000000
        String orderBy = "";
        if (desc == null) {
            desc = "02000000";
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
        if (desc.length() != 8) {
            return false;
        }
        int countZero = 0;
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) == '0') {
                countZero++;
            }
        }
        if (countZero != 7) {
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
