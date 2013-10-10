package models;

import com.uhuila.common.util.DateUtil;
import models.order.CheatedOrderSource;
import models.order.ECouponStatus;

import models.supplier.Supplier;

import operate.rbac.ContextedPermission;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

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
    public Long salesId;
    public String jobNumber;
    public Long supplierId = 0l;
    private Map<String, Object> paramMap = new HashMap<>();
    private Map<String, Object> paramMap1 = new HashMap<>();
    private Map<String, Object> paramMap2 = new HashMap<>();
    private Map<String, Object> paramMap3 = new HashMap<>();


    public Boolean hasSeeReportProfitRight;
    public Long operatorId;
    //排序字段
    public String desc;
    public String orderBy;
    public String orderByType;

    public void setDescFields() {

        // DESC 的值表示升降序，含n位，代表n个排序字段（不含订单编号,商品名称）， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 -1
        if (desc == null) {
            desc = "010000000";
        }
        // 获取最新的desc值
        String[] descs = desc.split(",");
        desc = descs[descs.length - 1].trim();
        if (isValidDesc(desc)) {
            //排序合法且没有优先指数，添加到condition 中
            int index = 0;
            // 定位排序属性
            for (int i = 0; i < desc.length(); i++) {
                if (desc.charAt(i) != '0') {
                    index = i;
                    break;
                }
            }
            String[] orderByFields = {"jobNumber", "totalAmount", "consumedAmount", "refundAmount", "cheatedOrderAmount", "netSalesAmount", "profit", "buyNumber", "grossMargin"};
            // 添加排序属性
            orderBy = orderByFields[index];
            // 添加升降序方式
            if (desc.charAt(index) == '1') {
                orderByType = "asc";
            } else {
                orderByType = "desc";
            }
        } else {
            // 一般排序，按总销售额
            orderBy = "totalAmount";
        }
    }

    /**
     * 判断排序字符串的合法性
     *
     * @param desc 排序字符串
     * @return
     */
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

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder(" where (r.order.status='PAID' " +
                " or r.order.status='SENT'" +
                " or r.order.status = 'PREPARED'" +
                " or r.order.status='UPLOADED'" +
                "  ) " +
                " and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");

        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code like :code");
            paramMap.put("code", code.trim() + "%");
        }

        if (beginAt != null) {
            condBuilder.append(" and r.order.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.createdAt < :createdAtEnd");
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
                condBuilder.append(" and 1=0");
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
            condBuilder.append(" and r.order.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.createdAt < :createdAtEnd");
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
                condBuilder.append(" and 1=0");
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
                " and e.isCheatedOrder = true and r.order=o and o.userId=b.id ");
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code like :code");
            paramMap.put("code", code.trim() + "%");
        }

        if (beginAt != null) {
            condBuilder.append(" and r.order.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.createdAt < :createdAtEnd");
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
                condBuilder.append(" and 1=0");
            }
        }
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }


    public String getFilterConsumedAt() {
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID' " +
                " and r.goods.isLottery=false and e.consumedAt is not null and e.status = models.order.ECouponStatus.CONSUMED " +
                " and  r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");
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
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
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
                condBuilder.append(" and 1=0");
            }
        }
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }


    public String getResalerFilter() {
        StringBuilder condBuilder = new StringBuilder(" where " +
                "  (r.order.status='PAID' " +
                " or r.order.status = 'PREPARED'" +
                " or r.order.status='UPLOADED'" +
                "  or r.order.status='SENT')" +
                " and r.goods.isLottery=false and r.order=o and o.userId=b.id" +
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
            condBuilder.append(" and r.order.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.createdAt < :createdAtEnd");
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
                condBuilder.append(" and 1=0");
            }
        }
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }


    /**
     * 本期购买，本期未消费退款
     *
     * @return
     */
    public String getSalesRefundFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and e.consumedAt is " +
                "null");
        paramMap2.put("status", ECouponStatus.REFUND);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and e.goods.shortName like :shortName");
            paramMap2.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and e.goods.code like :code");
            paramMap2.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap2.put("refundAtBegin", beginAt);
            condBuilder.append(" and e.createdAt >= :createdAtBegin");
            paramMap2.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap2.put("refundAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
            condBuilder.append(" and e.createdAt <= :createdAtEnd");
            paramMap2.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        if (hasSeeReportProfitRight != null && !hasSeeReportProfitRight) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap2.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 1=0");
            }
        }
        if (supplierId != 0) {
            condBuilder.append(" and e.goods.supplierId = :supplierId");
            paramMap2.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }

    /**
     * 本期购买，本期未消费退款
     *
     * @return
     */
    public String getFilterSalesRefundResaler() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and e.consumedAt is " +
                "null " +
                "  and r.order=o and o.userId=b.id ");
        paramMap2.put("status", ECouponStatus.REFUND);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and e.goods.shortName like :shortName");
            paramMap2.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and e.goods.code like :code");
            paramMap2.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap2.put("refundAtBegin", beginAt);
            condBuilder.append(" and e.createdAt >= :createdAtBegin");
            paramMap2.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap2.put("refundAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
            condBuilder.append(" and e.createdAt <= :createdAtEnd");
            paramMap2.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        if (hasSeeReportProfitRight != null && !hasSeeReportProfitRight) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap2.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 5 =:supplierIds");
                paramMap2.put("supplierIds", 6);
            }
        }
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap2.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }

    /**
     * 本期之前购买，本期未消费退款
     *
     * @return
     */
    public String getPreviousSalesRefundFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and e.consumedAt is " +
                "null");
        paramMap3.put("status", ECouponStatus.REFUND);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and e.goods.shortName like :shortName");
            paramMap3.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and e.goods.code like :code");
            paramMap3.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap3.put("refundAtBegin", beginAt);
            condBuilder.append(" and e.createdAt < :createdAtBegin");
            paramMap3.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap3.put("refundAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        if (hasSeeReportProfitRight != null && !hasSeeReportProfitRight) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap3.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 1=0");
            }
        }
        if (supplierId != 0) {
            condBuilder.append(" and e.goods.supplierId = :supplierId");
            paramMap3.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }


    /**
     * 本期之前购买，本期未消费退款
     *
     * @return
     */
    public String getFilterPreviousSalesRefundResaler() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and e.consumedAt is " +
                "null" +
                "  and r.order=o and o.userId=b.id ");
        paramMap3.put("status", ECouponStatus.REFUND);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and e.goods.shortName like :shortName");
            paramMap3.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and e.goods.code like :code");
            paramMap3.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap3.put("refundAtBegin", beginAt);
            condBuilder.append(" and e.createdAt < :createdAtBegin");
            paramMap3.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap3.put("refundAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        if (hasSeeReportProfitRight != null && !hasSeeReportProfitRight) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap3.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 5 =:supplierIds");
                paramMap3.put("supplierIds", 6);
            }
        }
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap3.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }


    /**
     * 本期消费，本期消费退款
     *
     * @return
     */
    public String getConsumedRefundFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and e.consumedAt is " +
                " not null");
        paramMap2.put("status", ECouponStatus.REFUND);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and e.goods.shortName like :shortName");
            paramMap2.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and e.goods.code like :code");
            paramMap2.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap2.put("refundAtBegin", beginAt);
            condBuilder.append(" and e.consumedAt >= :createdAtBegin");
            paramMap2.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap2.put("refundAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
            condBuilder.append(" and e.consumedAt <= :createdAtEnd");
            paramMap2.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        if (hasSeeReportProfitRight != null && !hasSeeReportProfitRight) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap2.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 1=0");
            }
        }
        if (supplierId != 0) {
            condBuilder.append(" and e.goods.supplierId = :supplierId");
            paramMap2.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }

    /**
     * 本期消费，本期消费退款
     *
     * @return
     */
    public String getFilterConsumedRefundResaler() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and e.consumedAt is " +
                " not null " +
                "  and r.order=o and o.userId=b.id ");
        paramMap2.put("status", ECouponStatus.REFUND);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and e.goods.shortName like :shortName");
            paramMap2.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and e.goods.code like :code");
            paramMap2.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap2.put("refundAtBegin", beginAt);
            condBuilder.append(" and e.consumedAt >= :createdAtBegin");
            paramMap2.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap2.put("refundAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
            condBuilder.append(" and e.consumedAt <= :createdAtEnd");
            paramMap2.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        if (hasSeeReportProfitRight != null && !hasSeeReportProfitRight) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap2.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 5 =:supplierIds");
                paramMap2.put("supplierIds", 6);
            }
        }
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap2.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }

    /**
     * 本期之前消费，本期消费退款
     *
     * @return
     */
    public String getPreviousConsumedRefundFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and e.consumedAt is " +
                " not null");
        paramMap3.put("status", ECouponStatus.REFUND);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and e.goods.shortName like :shortName");
            paramMap3.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and e.goods.code like :code");
            paramMap3.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap3.put("refundAtBegin", beginAt);
            condBuilder.append(" and e.consumedAt < :createdAtBegin");
            paramMap3.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap3.put("refundAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        if (hasSeeReportProfitRight != null && !hasSeeReportProfitRight) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap3.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 1=0");
            }
        }
        if (supplierId != 0) {
            condBuilder.append(" and e.goods.supplierId = :supplierId");
            paramMap3.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }


    /**
     * 本期之前消费，本期消费退款
     *
     * @return
     */
    public String getFilterPreviousConsumedRefundResaler() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and e.consumedAt is " +
                " not null" +
                "  and r.order=o and o.userId=b.id ");
        paramMap3.put("status", ECouponStatus.REFUND);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and e.goods.shortName like :shortName");
            paramMap3.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and e.goods.code like :code");
            paramMap3.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap3.put("refundAtBegin", beginAt);
            condBuilder.append(" and e.consumedAt < :createdAtBegin");
            paramMap3.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap3.put("refundAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        if (hasSeeReportProfitRight != null && !hasSeeReportProfitRight) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap3.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 5 =:supplierIds");
                paramMap3.put("supplierIds", 6);
            }
        }
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap3.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public Map<String, Object> getParamMap1() {
        return paramMap1;
    }

    public Map<String, Object> getParamMap2() {
        return paramMap2;
    }

    public Map<String, Object> getParamMap3() {
        return paramMap3;
    }


    public String getFilterOfPeopleEffect() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId =s.id and s.deleted=0 and s.salesId=o.id and (r.order.status='PAID' or r.order.status='SENT' " +
                "or r.order.status = 'PREPARED' or r.order.status='UPLOADED') and r.goods.isLottery=false");
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
            condBuilder.append(" and r.order.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.createdAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(endAt));
        }
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }

        return condBuilder.toString();

    }


    public String getRefundFilterOfPeopleEffect(ECouponStatus status) {
        paramMap1 = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.goods.supplierId=s.id and s.deleted=0 and s.salesId=o.id and e.status=:status and e.goods.isLottery=false");
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
        if (supplierId != 0) {
            condBuilder.append(" and e.goods.supplierId = :supplierId");
            paramMap1.put("supplierId", supplierId);
        }
        return condBuilder.toString();
    }

    public String getOfflineGross() {
        paramMap = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder(" where e.supplier=s and s.deleted=0 and s.salesId=ou.id ");
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
            paramMap.put("salesId", salesId);
        }
        if (StringUtils.isNotBlank(userName)) {
            condBuilder.append(" and ou.userName like :userName");
            paramMap.put("userName", "%" + userName + "%");
        }
        if (StringUtils.isNotBlank(jobNumber)) {
            condBuilder.append(" and ou.jobNumber=:jobNumber");
            paramMap.put("jobNumber", jobNumber);
        }
        if (beginAt != null) {
            condBuilder.append(" and e.receivedAt >= :receivedAtBegin");
            paramMap.put("receivedAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.receivedAt <= :receivedAtEnd");
            paramMap.put("receivedAtEnd", DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();
    }

    public String getFilterCheatedOrderOfPeopleEffect(CheatedOrderSource cheatedOrderSource) {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.goods.supplierId=s.id and s.deleted=0 and s.salesId=ou.id and r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and e.isCheatedOrder = true ");
        paramMap1 = new HashMap<>();
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (cheatedOrderSource != null) {
            condBuilder.append(" and e.cheatedOrderSource =:cheatedOrderSource");
            paramMap1.put("cheatedOrderSource", cheatedOrderSource);
        }
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
            paramMap1.put("salesId", salesId);
        }
        if (StringUtils.isNotBlank(userName)) {
            condBuilder.append(" and ou.userName like :userName");
            paramMap1.put("userName", "%" + userName + "%");
        }
        if (StringUtils.isNotBlank(jobNumber)) {
            condBuilder.append(" and ou.jobNumber=:jobNumber");
            paramMap1.put("jobNumber", jobNumber);
        }
        if (beginAt != null) {
            condBuilder.append(" and e.cheatedAt >= :createdAtBegin");
            paramMap1.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.cheatedAt < :createdAtEnd");
            paramMap1.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap1.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }

    public void sort(List resultList) {
        Collections.sort(resultList, new Comparator<SalesReport>() {
            @Override
            public int compare(SalesReport o1, SalesReport o2) {
                String o1_jobNumber = o1.operateUser.jobNumber == null ? "" : o1.operateUser.jobNumber;
                String o2_jobNumber = o2.operateUser.jobNumber == null ? "" : o2.operateUser.jobNumber;
                if ("jobNumber".equals(orderBy)) {
                    if ("desc".equals(orderByType)) {
                        return o1_jobNumber.compareTo(o2_jobNumber);
                    } else {
                        return o2_jobNumber.compareTo(o1_jobNumber);
                    }
                } else if ("totalAmount".equals(orderBy)) {
                    BigDecimal o1_totalAmount = o1.totalAmount == null ? BigDecimal.ZERO : o1.totalAmount;
                    BigDecimal o2_totalAmount = o2.totalAmount == null ? BigDecimal.ZERO : o2.totalAmount;
                    if ("desc".equals(orderByType)) {
                        return o1_totalAmount.compareTo(o2_totalAmount);
                    } else {
                        return o2_totalAmount.compareTo(o1_totalAmount);
                    }
                } else if ("consumedAmount".equals(orderBy)) {
                    BigDecimal o1_amount = o1.consumedAmount == null ? BigDecimal.ZERO : o1.consumedAmount;
                    BigDecimal o2_amount = o2.consumedAmount == null ? BigDecimal.ZERO : o2.consumedAmount;
                    if ("desc".equals(orderByType)) {
                        return o1_amount.compareTo(o2_amount);
                    } else {
                        return o2_amount.compareTo(o1_amount);
                    }
                } else if ("refundAmount".equals(orderBy)) {
                    BigDecimal o1_amount = o1.refundAmount == null ? BigDecimal.ZERO : o1.refundAmount;
                    BigDecimal o2_amount = o2.refundAmount == null ? BigDecimal.ZERO : o2.refundAmount;
                    if ("desc".equals(orderByType)) {
                        return o1_amount.compareTo(o2_amount);
                    } else {
                        return o2_amount.compareTo(o1_amount);
                    }
                } else if ("cheatedOrderAmount".equals(orderBy)) {
                    BigDecimal o1_amount = o1.cheatedOrderAmount == null ? BigDecimal.ZERO : o1.cheatedOrderAmount;
                    BigDecimal o2_amount = o2.cheatedOrderAmount == null ? BigDecimal.ZERO : o2.cheatedOrderAmount;
                    if ("desc".equals(orderByType)) {
                        return o1_amount.compareTo(o2_amount);
                    } else {
                        return o2_amount.compareTo(o1_amount);
                    }
                } else if ("netSalesAmount".equals(orderBy)) {
                    BigDecimal o1_amount = o1.netSalesAmount == null ? BigDecimal.ZERO : o1.netSalesAmount;
                    BigDecimal o2_amount = o2.netSalesAmount == null ? BigDecimal.ZERO : o2.netSalesAmount;
                    if ("desc".equals(orderByType)) {
                        return o1_amount.compareTo(o2_amount);
                    } else {
                        return o2_amount.compareTo(o1_amount);
                    }
                } else if ("profit".equals(orderBy)) {
                    BigDecimal o1_amount = o1.profit == null ? BigDecimal.ZERO : o1.profit;
                    BigDecimal o2_amount = o2.profit == null ? BigDecimal.ZERO : o2.profit;
                    if ("desc".equals(orderByType)) {
                        return o1_amount.compareTo(o2_amount);
                    } else {
                        return o2_amount.compareTo(o1_amount);
                    }
                } else if ("buyNumber".equals(orderBy)) {
                    Long o1_buyNumber = o1.buyNumber == null ? 0l : o1.buyNumber;
                    Long o2_buyNumber = o2.buyNumber == null ? 0l : o2.buyNumber;
                    if ("desc".equals(orderByType)) {
                        return o1_buyNumber.compareTo(o2_buyNumber);
                    } else {
                        return o2_buyNumber.compareTo(o1_buyNumber);
                    }
                } else if ("grossMargin".equals(orderBy)) {
                    BigDecimal o1_amount = o1.grossMargin == null ? BigDecimal.ZERO : o1.grossMargin;
                    BigDecimal o2_amount = o2.grossMargin == null ? BigDecimal.ZERO : o2.grossMargin;
                    if ("desc".equals(orderByType)) {
                        return o1_amount.compareTo(o2_amount);
                    } else {
                        return o2_amount.compareTo(o1_amount);
                    }
                }

                return o1_jobNumber.compareTo(o2_jobNumber);
            }
        }

        );
    }
}
