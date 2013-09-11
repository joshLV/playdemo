package models;

import models.accounts.AccountType;
import models.resale.Resaler;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 渠道大类查询条件
 * <p/>
 * User: wangjia
 * Date: 12-12-20
 * Time: 下午5:57
 */
public class ChannelCategoryReportCondition implements Serializable {
    public Date beginAt = com.uhuila.common.util.DateUtil.getBeginOfDay();
    public Date endAt = com.uhuila.common.util.DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    public AccountType accountType;
    private Map<String, Object> paramMap = new HashMap<>();
    public Boolean hasSeeReportProfitRight;
    public Long operatorId;
    //排序字段
    public String desc;
    public int orderByIndex;
    public String orderByType;
    Map<String, BigDecimal> comparedMap = new HashMap<>();
    public String goodsCode;

    public String getFilterPaidAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder("and (r.order.status='PAID' or r.order.status = 'SENT' " +
                "or r.order.status = 'PREPARED' or r.order.status='UPLOADED') " +
                "and r.goods.isLottery=false and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED"
        );

        if (type == AccountType.CONSUMER) {
            condBuilder.append(" and r.order.userId = :yibaiquanId");
            paramMap.put("yibaiquanId", Resaler.getYibaiquan().id);
        } else {
            condBuilder.append(" and r.order.userId <> :yibaiquanId");
            paramMap.put("yibaiquanId", Resaler.getYibaiquan().id);
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
        if (StringUtils.isNotBlank(goodsCode)) {
            condBuilder.append(" and r.goods.code like :goodsCode");
            paramMap.put("goodsCode", goodsCode.trim() + "%");
        }
        return condBuilder.toString();
    }

    public String getFilterRealSendAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder("(r.order.status='PAID' or r.order.status='SENT')  " +
                "and r.goods.isLottery=false and r.goods.materialType=models.sales.MaterialType.REAL" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order.deliveryType=models.order.DeliveryType.LOGISTICS");

        if (type == AccountType.CONSUMER) {
            condBuilder.append(" and r.order.userId = :yibaiquanId");
            paramMap.put("yibaiquanId", Resaler.getYibaiquan().id);
        } else {
            condBuilder.append(" and r.order.userId <> :yibaiquanId");
            paramMap.put("yibaiquanId", Resaler.getYibaiquan().id);
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
        if (StringUtils.isNotBlank(goodsCode)) {
            condBuilder.append(" and r.goods.code like :goodsCode");
            paramMap.put("goodsCode", goodsCode.trim() + "%");
        }
        return condBuilder.toString();
    }


    public String getFilterConsumedAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID' and r.goods.isLottery=false and e.status = models.order.ECouponStatus.CONSUMED" +
                " and  r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");

        if (type == AccountType.CONSUMER) {
            condBuilder.append(" and r.order.userId = :yibaiquanId");
            paramMap.put("yibaiquanId", Resaler.getYibaiquan().id);
        } else {
            condBuilder.append(" and r.order.userId <> :yibaiquanId");
            paramMap.put("yibaiquanId", Resaler.getYibaiquan().id);
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
        if (StringUtils.isNotBlank(goodsCode)) {
            condBuilder.append(" and r.goods.code like :goodsCode");
            paramMap.put("goodsCode", goodsCode.trim() + "%");
        }
        return condBuilder.toString();
    }

    public String getFilterRefundAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" and r.order.status='PAID'" +
                " and e.goods.isLottery=false and e.status = models.order.ECouponStatus.REFUND" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");

        if (type == AccountType.CONSUMER) {
            condBuilder.append(" and e.order.userId = :yibaiquanId");
            paramMap.put("yibaiquanId", Resaler.getYibaiquan().id);
        } else {
            condBuilder.append(" and e.order.userId <> :yibaiquanId");
            paramMap.put("yibaiquanId", Resaler.getYibaiquan().id);
        }

        if (beginAt != null) {
            condBuilder.append(" and e.refundAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and e.refundAt < :createdAtEnd");
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
        if (StringUtils.isNotBlank(goodsCode)) {
            condBuilder.append(" and r.goods.code like :goodsCode");
            paramMap.put("goodsCode", goodsCode.trim() + "%");
        }
        return condBuilder.toString();
    }

    public String getFilterRealRefundAt(AccountType type) {
        StringBuilder condBuilder = new StringBuilder(" where r.order.status='SENT' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED");

        if (type == AccountType.CONSUMER) {
            condBuilder.append(" and r.order.userId = :yibaiquanId");
            paramMap.put("yibaiquanId", Resaler.getYibaiquan().id);
        } else {
            condBuilder.append(" and r.order.userId <> :yibaiquanId");
            paramMap.put("yibaiquanId", Resaler.getYibaiquan().id);
        }

        if (beginAt != null) {
            condBuilder.append(" and r.order.refundAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.refundAt < :createdAtEnd");
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
        if (StringUtils.isNotBlank(goodsCode)) {
            condBuilder.append(" and r.goods.code like :goodsCode");
            paramMap.put("goodsCode", goodsCode.trim() + "%");
        }
        return condBuilder.toString();
    }


    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setDescFields() {
        // DESC 的值表示升降序，含11位，代表11个排序字段， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 2000000
        String orderBy = "";
        if (desc == null) {
            desc = "2000000";
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
        if (desc.length() != 7) {
            return false;
        }
        int countZero = 0;
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) == '0') {
                countZero++;
            }
        }
        if (countZero != 6) {
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
