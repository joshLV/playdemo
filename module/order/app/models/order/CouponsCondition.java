package models.order;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CouponsCondition {
    public Date createdAtBegin;
    public Date createdAtEnd;

    public Date consumedAtBegin;
    public Date consumedAtEnd;

    public Date refundAtBegin;
    public Date refundAtEnd;

    public ECouponStatus status;
    public ECouponStatus excludeStatus;
    public String goodsName;
    public String orderNumber;
    public String phone;
    public Long userId;
    public AccountType accountType;
    public Supplier supplier;
    public String shopLike;
    public String jobNumber;
    public String eCouponSn;
    public VerifyCouponType verifyType;
    private Map<String, Object> paramMap = new HashMap<>();

    public String getOrderByExpress() {
        return "e.createdAt desc";
    }

    /**
     * 券查询条件
     *
     * @return sql 查询条件
     */
    public String getFilter() {
        StringBuilder sql = new StringBuilder();
        sql.append(" 1=1 ");
        if (userId != null && accountType != null) {
            sql.append(" and e.order.userId = :userId and e.order.userType = :userType");
            paramMap.put("userId", userId);
            paramMap.put("userType", accountType);
        }

        if (StringUtils.isNotBlank(eCouponSn)) {
            sql.append(" and e.eCouponSn like :eCouponSn");
            paramMap.put("eCouponSn", "%" + eCouponSn + "%");
        }

        if (createdAtBegin != null) {
            sql.append(" and e.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", createdAtBegin);
        }

        if (createdAtEnd != null) {
            sql.append(" and e.createdAt <= :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }

        if (consumedAtBegin != null) {
            sql.append(" and e.consumedAt >= :consumedAtBegin");
            paramMap.put("consumedAtBegin", consumedAtBegin);
        }

        if (consumedAtEnd != null) {
            sql.append(" and e.consumedAt <= :consumedAtEnd");
            paramMap.put("consumedAtEnd", DateUtil.getEndOfDay(consumedAtEnd));
        }

        if (refundAtBegin != null) {
            sql.append(" and e.refundAt >= :refundAtBegin");
            paramMap.put("refundAtBegin", refundAtBegin);
        }

        if (refundAtEnd != null) {
            sql.append(" and e.refundAt <= :refundAtEnd");
            paramMap.put("refundAtEnd", DateUtil.getEndOfDay(refundAtEnd));
        }

        if (StringUtils.isNotBlank(jobNumber)) {
            sql.append(" and e.supplierUser.jobNumber=:jobNumber");
            paramMap.put("jobNumber", jobNumber);
        }

        if (StringUtils.isNotBlank(goodsName)) {
            sql.append(" and e.goods.name like :name");
            paramMap.put("name", "%" + goodsName + "%");
        }

        if (StringUtils.isNotBlank(shopLike)) {
            sql.append(" and (e.shop.name like :shopLike or e.shop.address like :shopLike)");
            paramMap.put("shopLike", "%" + shopLike + "%");
        }

        if (status != null) {
            sql.append(" and e.status = :status");
            paramMap.put("status", status);
        }

         if ( verifyType!= null) {
            sql.append(" and e.verifyType = :verifyType");
            paramMap.put("verifyType", verifyType);
        }

        if (excludeStatus != null) {
            sql.append(" and e.status != :excludeStatus");
            paramMap.put("excludeStatus", excludeStatus);
        }

        if (StringUtils.isNotBlank(orderNumber)) {
            sql.append(" and e.order.orderNumber like :orderNumber");
            paramMap.put("orderNumber", "%" + orderNumber + "%");
        }

        if (StringUtils.isNotBlank(phone)) {
            sql.append(" and e.orderItems.phone like :phone");
            paramMap.put("phone", "%" + phone + "%");
        }

        return sql.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
