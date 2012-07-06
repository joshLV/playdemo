package models;

import com.uhuila.common.util.DateUtil;
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

    public Supplier supplier;
    public Date createdAtBegin = DateUtil.getYesterday();
    public Date createdAtEnd = DateUtil.getEndOfDay(DateUtil.getYesterday());
    public String orderBy = "r.createdAt";
    public String orderByType = "DESC";
    public String interval = "0d";

    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder("r.order.status='PAID' and s.id=r.goods.supplierId");
        if (createdAtBegin != null) {
            condBuilder.append(" and r.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            condBuilder.append(" and r.createdAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }

        if (supplier != null && supplier.id != 0) {
            condBuilder.append(" and s = :supplier");
            paramMap.put("supplier", supplier);
            System.out.println("supplier.id:" + supplier.id);
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

    public String getOrderByExpress() {
        String orderType = StringUtils.isBlank(orderByType) ? "DESC" : orderByType;
        return StringUtils.isBlank(orderBy) ? "r.createdAt DESC" : orderBy + " " + orderType;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
