package models;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.order.ECouponStatus;
import operate.rbac.ContextedPermission;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
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
public class PeopleEffectCategoryReportCondition implements Serializable {
    public Date beginAt = com.uhuila.common.util.DateUtil.getBeginOfDay();
    public Date endAt = com.uhuila.common.util.DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    public String userName;
    public String jobNumber;
    public AccountType accountType;
    public Long salesId;
    public String categoryCode;
    //排序字段
    public String desc;
    public String orderBy;
    public String orderByType;
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilterPaidAt() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and r.goods.supplierId = s.id and s.deleted=0 and s.salesId=ou.id and r.order.status='PAID' " +
                "and r.goods.isLottery=false and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED"
        );

        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
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

    public String getFilterRealSendAt() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId =s.id and s.deleted=0 and s.salesId=ou.id and (r.order.status='PAID' or r.order.status='SENT')  " +
                "and r.goods.isLottery=false and r.goods.materialType=models.sales.MaterialType.REAL" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order.deliveryType=models.order.DeliveryType.LOGISTICS");

        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
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

    public String getResalerFilterOfPeopleEffect() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.supplierId =s.id and s.deleted=0 and s.salesId=ou.id and r.order.userType=models.accounts.AccountType.RESALER " +
                " and (r.order.status='PAID' or r.order.status='SENT')" +
                " and r.goods.isLottery=false and r.order=o and o.userId=b.id");
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
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

    public String getECouponFilterOfPeopleEffect(ECouponStatus status) {
        paramMap1 = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and r.goods.supplierId =s.id and e.goods.supplierId=s.id and s.deleted=0 and s.salesId=ou.id and e.status=:status and e.goods.isLottery=false");
        paramMap1.put("status", status);
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
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
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.goods.supplierId=s.id and s.deleted=0 and s.salesId=ou.id and r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and e.isCheatedOrder = true ");
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
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
            paramMap.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();
    }

    public String getFilterCheatedOrderResalerOfPeopleEffect() {
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.goods.supplierId=s.id and s.deleted=0 and s.salesId=ou.id and r.order.status='PAID' and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and e.isCheatedOrder = true and r.order.userType=models.accounts.AccountType.RESALER and r.order=o and o.userId=b.id ");
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
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
            paramMap.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }
        return condBuilder.toString();
    }

    public String getFilterRefundResalerOfPeopleEffect() {
        paramMap1 = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder(" where e.orderItems=r and e.goods.supplierId=s.id and s.deleted=0 and s.salesId=ou.id and e.status=:status and e.goods.isLottery=false" +
                " and e.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED" +
                " and r.order.userType=models.accounts.AccountType.RESALER and r.order=o and o.userId=b.id ");
        paramMap1.put("status", ECouponStatus.REFUND);
        Boolean hasSeeReportProfitRight = ContextedPermission.hasPermission("SEE_OPERATION_REPORT_PROFIT");
        if (!hasSeeReportProfitRight) {
            condBuilder.append(" and ou.id =:salesId");
            paramMap1.put("salesId", salesId);
        }
        if (StringUtils.isNotBlank(userName)) {
            condBuilder.append(" and ou.userName like :shortName");
            paramMap1.put("shortName", "%" + userName + "%");
        }
        if (StringUtils.isNotBlank(jobNumber)) {
            condBuilder.append(" and ou.jobNumber= :jobNumber");
            paramMap1.put("jobNumber", jobNumber);
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

    public void setDescFields() {

        // DESC 的值表示升降序，含n位，代表n个排序字段， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 -1
        if (desc == null) {
            desc = "010000";
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
            String[] orderByFields = {"jobNumber", "totalAmount", "refundAmount", "consumedAmount", "profit", "grossMargin"};
            // 添加排序属性
            orderBy = orderByFields[index];
            // 添加升降序方式
            if (desc.charAt(index) == '1') {
                orderByType = "asc";
            } else {
                orderByType = "desc";
            }
        } else {
            // 一般排序，按售出总金额
            orderBy = "jobNumber";
        }
    }

    /**
     * 判断排序字符串的合法性
     *
     * @param desc 排序字符串
     * @return
     */
    public static boolean isValidDesc(String desc) {
        if (desc.length() != 6) {
            return false;
        }
        int countZero = 0;
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) == '0') {
                countZero++;
            }
        }
        if (countZero != 5) {
            return false;
        }
        for (int i = 0; i < desc.length(); i++) {
            if (desc.charAt(i) != '0' && desc.charAt(i) != '1' && desc.charAt(i) != '2') {
                return false;
            }
        }
        return true;
    }

    public void sort(List resultList) {
        Collections.sort(resultList, new Comparator<PeopleEffectCategoryReport>() {
            @Override
            public int compare(PeopleEffectCategoryReport o1, PeopleEffectCategoryReport o2) {
                String o1_jobNumber = StringUtils.trimToEmpty(o1.operateUser.jobNumber);
                String o2_jobNumber = StringUtils.trimToEmpty(o2.operateUser.jobNumber);
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
                    BigDecimal o1_amount = o1.totalConsumedPrice == null ? BigDecimal.ZERO : o1.totalConsumedPrice;
                    BigDecimal o2_amount = o2.totalConsumedPrice == null ? BigDecimal.ZERO : o2.totalConsumedPrice;
                    if ("desc".equals(orderByType)) {
                        return o1_amount.compareTo(o2_amount);
                    } else {
                        return o2_amount.compareTo(o1_amount);
                    }
                } else if ("refundAmount".equals(orderBy)) {
                    System.out.println("===inini>>");
                    BigDecimal o1_amount = o1.totalRefundPrice == null ? BigDecimal.ZERO : o1.totalRefundPrice;
                    BigDecimal o2_amount = o2.totalRefundPrice == null ? BigDecimal.ZERO : o2.totalRefundPrice;
                    if ("desc".equals(orderByType)) {
                        return o1_amount.compareTo(o2_amount);
                    } else {
                        return o2_amount.compareTo(o1_amount);
                    }
                } else if ("profit".equals(orderBy)) {
                    BigDecimal o1_amount = o1.netProfit == null ? BigDecimal.ZERO : o1.netProfit;
                    BigDecimal o2_amount = o2.netProfit == null ? BigDecimal.ZERO : o2.netProfit;
                    if ("desc".equals(orderByType)) {
                        return o1_amount.compareTo(o2_amount);
                    } else {
                        return o2_amount.compareTo(o1_amount);
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

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public Map<String, Object> getParamMap1() {
        return paramMap1;
    }

}
