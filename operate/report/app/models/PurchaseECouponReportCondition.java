package models;

import com.uhuila.common.util.DateUtil;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.Logger;

import java.io.Serializable;
import java.util.*;

import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import com.uhuila.common.util.DateUtil;

/**
 * 报表查询条件.
 * <p/>
 * User: sujie
 * Date: 5/17/12
 * Time: 5:41 PM
 */
public class PurchaseECouponReportCondition implements Serializable {
    public String shopLike;
    public String goodsLike;
    public String supplierLike;

    public Supplier supplier;
    public Date createdAtBegin = DateUtil.getBeginOfDay();
    public Date createdAtEnd = DateUtil.getEndOfDay(new Date());
    public String orderBy = "r.createdAt";
    public String orderByType = "DESC";
    public String interval = "0d";

    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter(Long id, Boolean right) {
        StringBuilder condBuilder = new StringBuilder("r.status='CONSUMED'"); //只统计已经消费的
        if (createdAtBegin != null) {
            condBuilder.append(" and r.consumedAt >= :consumedAtBegin");
            paramMap.put("consumedAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            condBuilder.append(" and r.consumedAt < :consumedAtEnd");
            paramMap.put("consumedAtEnd", DateUtil.getEndOfDay(createdAtEnd)); //TODO: 表单名改成consumedAt
        }

        if (supplier != null && supplier.id != 0) {
            condBuilder.append(" and r.shop.supplierId = :supplier");
            paramMap.put("supplier", supplier.id);
            Logger.debug("supplier.id:" + supplier.id);
        }

        if (supplier != null && supplier.id == 0 && !right) {
            List<Supplier> suppliers = Supplier.find("salesId=?", id).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            condBuilder.append(" and r.goods.supplierId in (:supplierIds)");
            paramMap.put("supplierIds", supplierIds);
        }

        if (supplier == null && !right) {
            List<Supplier> suppliers = Supplier.find("salesId=?", id).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            condBuilder.append(" and r.goods.supplierId in (:supplierIds)");
            paramMap.put("supplierIds", supplierIds);
        }


        if (StringUtils.isNotBlank(goodsLike)) {
            condBuilder.append(" and r.goods.shortName like :goodsLike");
            paramMap.put("goodsLike", "%" + goodsLike + "%");
        }

        if (StringUtils.isNotBlank(shopLike)) {
            condBuilder.append(" and r.shop.name like :shopLike");
            paramMap.put("shopLike", "%" + shopLike + "%");
        }
        if (StringUtils.isNotBlank(supplierLike)) {
            condBuilder.append(" and r.shop.supplier.fullName like :supplierLike");
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
