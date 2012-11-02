package models;

import com.uhuila.common.util.DateUtil;
import controllers.OperateRbac;
import models.order.ECouponStatus;
import models.sales.MaterialType;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    private Map<String, Object> paramMap = new HashMap<>();
    private Map<String, Object> paramMap1 = new HashMap<>();

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

        System.out.println("condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }

    public String getNetSalesFilter(Long id, Boolean right) {
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

        if (supplier != null && supplier.id == 0 && !right) {
            condBuilder.append(" and s.salesId = :salesId");
            paramMap.put("salesId", id);
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

//        System.out.println("condBuilder.toString():" + condBuilder.toString());
//        System.out.println("condBuilder>>>>>>>>>>>>>>>>>>>>>>>>>>." + condBuilder.toString());
        return condBuilder.toString();
    }

    public String getRefundFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.status=:status and e.goods.isLottery=false");
        paramMap1.put("status", ECouponStatus.REFUND);
        if (createdAtBegin != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap1.put("refundAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap1.put("refundAtEnd", DateUtil.getEndOfDay(createdAtEnd));
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
}
