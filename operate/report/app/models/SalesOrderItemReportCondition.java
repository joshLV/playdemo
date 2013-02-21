package models;

import com.uhuila.common.util.DateUtil;
import models.operator.OperateUser;
import models.order.ECouponStatus;
import models.sales.MaterialType;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * 报表查询条件.
 * <p/>
 * User: sujie
 * Date: 5/17/12
 * Time: 5:41 PM
 */
public class SalesOrderItemReportCondition implements Serializable {
    public String shopLike;
    public String goodsLike;
    public String supplierLike;
    public MaterialType materialType;
    public Supplier supplier;
    public Date createdAtBegin = DateUtil.getBeginOfDay();
    public Date createdAtEnd = DateUtil.getEndOfDay(new Date());
    public String orderBy = "r.createdAt";
    public String orderByType = "DESC";
    public String interval = "-1d";
    public Long operatorId;
    public Boolean hasSeeAllSupplierPermission;
    public String supplierCode;

    private Map<String, Object> paramMap = new HashMap<>();
    private Map<String, Object> paramMap1 = new HashMap<>();

    //排序字段
    public String desc;

    public void setDescFields() {

        // DESC 的值表示升降序，含n位，代表n个排序字段（不含订单编号,商品名称）， 1 为升序， 2 为降序， 0 为不排序
        // 当无排序参数时，初始化 -1
        if (desc == null) {
            desc = "000100";
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
            String[] orderByFields = {"jobNumber", "fullName", "code", "salesAmount", "refundAmount", "netSalesAmount"};
            // 添加排序属性
            orderBy = orderByFields[index];
            // 添加升降序方式
            if (desc.charAt(index) == '1') {
                orderByType = "desc";
            } else {
                orderByType = "asc";
            }
        } else {
            // 一般排序，按总销售额
            orderBy = "salesAmount";
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

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder("(r.order.status='PAID' or r.order.status='SENT') and s.id=r.goods.supplierId and r.goods.isLottery=false");
        //condBuilder.append(" and ");
        if (createdAtBegin != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }

        if (supplier != null && supplier.id != 0) {
            condBuilder.append(" and s = :supplier");
            paramMap.put("supplier", supplier);
        }

        if (hasSeeAllSupplierPermission != null && !hasSeeAllSupplierPermission) {
            condBuilder.append(" and s.salesId = :salesId");
            paramMap.put("salesId", operatorId);
        }


        if (materialType != null) {
            condBuilder.append(" and r.goods.materialType = :materialType");
            paramMap.put("materialType", materialType);
        }
        if (StringUtils.isNotBlank(goodsLike)) {
            condBuilder.append(" and r.goods.name like :goodsLike");
            paramMap.put("goodsLike", "%" + goodsLike + "%");
        }

        if (StringUtils.isNotBlank(shopLike)) {
            condBuilder.append(" and r.shop.name like :shopLike");
            paramMap.put("shopLike", "%" + shopLike + "%");
        }

        if (StringUtils.isNotBlank(supplierLike)) {
            condBuilder.append(" and s.fullName like :supplierLike");
            paramMap.put("supplierLike", "%" + supplierLike + "%");
        }

        return condBuilder.toString();
    }

    public String getNetSalesFilter() {
        StringBuilder condBuilder = new StringBuilder("(r.order.status='PAID' or r.order.status='SENT') and s.id=r.goods.supplierId and r.goods.isLottery=false");

        if (createdAtBegin != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }

        if (supplier != null && supplier.id != 0) {
            condBuilder.append(" and s = :supplier");
            paramMap.put("supplier", supplier);
        }

        if (hasSeeAllSupplierPermission != null && !hasSeeAllSupplierPermission) {
            condBuilder.append(" and s.salesId = :salesId");
            paramMap.put("salesId", operatorId);
        }
        if (StringUtils.isNotBlank(supplierCode)) {
            condBuilder.append(" and s.code = :supplierCode");
            paramMap.put("supplierCode", supplierCode);
        }


        if (materialType != null) {
            condBuilder.append(" and r.goods.materialType = :materialType");
            paramMap.put("materialType", materialType);
        }
        if (StringUtils.isNotBlank(goodsLike)) {
            condBuilder.append(" and r.goods.name like :goodsLike");
            paramMap.put("goodsLike", "%" + goodsLike + "%");
        }

        if (StringUtils.isNotBlank(shopLike)) {
            condBuilder.append(" and r.shop.name like :shopLike");
            paramMap.put("shopLike", "%" + shopLike + "%");
        }

        if (StringUtils.isNotBlank(supplierLike)) {
            condBuilder.append(" and s.fullName like :supplierLike");
            paramMap.put("supplierLike", "%" + supplierLike + "%");
        }

        return condBuilder.toString();
    }

    public String getRefundFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.status=:status and e.goods.isLottery=false and s.id=r.goods.supplierId and e.orderItems.id=r.id ");
        paramMap1.put("status", ECouponStatus.REFUND);
        if (createdAtBegin != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap1.put("refundAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap1.put("refundAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        if (supplier != null && supplier.id != 0) {
            condBuilder.append(" and s = :supplier");
            paramMap1.put("supplier", supplier);
        }
        if (StringUtils.isNotBlank(supplierCode)) {
            condBuilder.append(" and s.code = :supplierCode");
            paramMap1.put("supplierCode", supplierCode);
        }

        return condBuilder.toString();
    }

    public String getOrderByExpress() {
        String orderType = StringUtils.isBlank(orderByType) ? "DESC" : orderByType;
        return StringUtils.isBlank(orderBy) ? "r.createdAt DESC" : orderBy + " " + orderType;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public Map<String, Object> getParamMap1() {
        return paramMap1;
    }

    public void sort(List resultList) {
        Collections.sort(resultList, new Comparator<SalesOrderItemReport>() {
            @Override
            public int compare(SalesOrderItemReport o1, SalesOrderItemReport o2) {
                OperateUser ou1 = OperateUser.findById(o1.supplier.salesId);
                OperateUser ou2 = OperateUser.findById(o2.supplier.salesId);
                String o1_jobNumber = ou1.jobNumber;
                String o2_jobNumber = ou2.jobNumber;
                if ("jobNumber".equals(orderBy)) {
                    if ("desc".equals(orderByType)) {
                        return o2_jobNumber.compareTo(o1_jobNumber);
                    } else {
                        return o1_jobNumber.compareTo(o2_jobNumber);
                    }
                } else if ("fullName".equals(orderBy)) {
                    String o1_fullName = o1.supplier.fullName;
                    String o2_fullName = o2.supplier.fullName;
                    if ("desc".equals(orderByType)) {
                        return o2_fullName.compareTo(o1_fullName);
                    } else {
                        return o1_fullName.compareTo(o2_fullName);
                    }
                } else if ("code".equals(orderBy)) {
                    String o1_code = o1.supplier.code;
                    String o2_code = o2.supplier.code;
                    if ("desc".equals(orderByType)) {
                        return o2_code.compareTo(o1_code);
                    } else {
                        return o1_code.compareTo(o2_code);
                    }
                } else if ("salesAmount".equals(orderBy)) {
                    BigDecimal o1_salesAmount = o1.salesAmount == null ? BigDecimal.ZERO : o1.salesAmount;
                    BigDecimal o2_salesAmount = o2.salesAmount == null ? BigDecimal.ZERO : o2.salesAmount;
                    if ("desc".equals(orderByType)) {
                        return o2_salesAmount.compareTo(o1_salesAmount);
                    } else {
                        return o1_salesAmount.compareTo(o2_salesAmount);
                    }
                } else if ("refundAmount".equals(orderBy)) {
                    BigDecimal o1_refundAmount = o1.refundAmount == null ? BigDecimal.ZERO : o1.refundAmount;
                    BigDecimal o2_refundAmount = o2.refundAmount == null ? BigDecimal.ZERO : o2.refundAmount;
                    if ("desc".equals(orderByType)) {
                        return o2_refundAmount.compareTo(o1_refundAmount);
                    } else {
                        return o1_refundAmount.compareTo(o2_refundAmount);
                    }
                } else if ("netSalesAmount".equals(orderBy)) {
                    BigDecimal o1_netSalesAmount = o1.netSalesAmount == null ? BigDecimal.ZERO : o1.netSalesAmount;
                    BigDecimal o2_netSalesAmount = o2.netSalesAmount == null ? BigDecimal.ZERO : o2.netSalesAmount;
                    if ("desc".equals(orderByType)) {
                        return o2_netSalesAmount.compareTo(o1_netSalesAmount);
                    } else {
                        return o1_netSalesAmount.compareTo(o2_netSalesAmount);
                    }
                }

                return o1_jobNumber.compareTo(o2_jobNumber);
            }
        }

        );
    }
}
