package models.order;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.resale.Resaler;
import models.sales.Brand;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;

public class CouponsCondition implements Serializable {

//    private static final long serialVersionUID = 1632320311301L;

    public Date createdAtBegin;
    public Date createdAtEnd;

    public Date consumedAtBegin;
    public Date consumedAtEnd;

    public Date refundAtBegin;
    public Date refundAtEnd;
    public Long brandId = 0l;
    public ECouponStatus status;
    public ECouponStatus excludeStatus;
    public String goodsName;
    public String orderNumber;
    public String phone;
    public String allSearch;
    public Long userId;
    public AccountType accountType;
    public Supplier supplier;
    public String shopLike;
    public String jobNumber;
    public String eCouponSn;
    public VerifyCouponType verifyType;
    private Map<String, Object> paramMap = new HashMap<>();
    public String searchItems;
    public String searchKey;
    public boolean isLottery;
    public Date paidAtBegin;
    public Date paidAtEnd;
    public String userName;

    public Long operatorId;
    public Boolean hasSeeAllSupplierPermission;

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
        if (paidAtBegin != null) {
            sql.append(" and e.order.paidAt>= :paidAtBegin");
            paramMap.put("paidAtBegin", paidAtBegin);
        }

        if (paidAtEnd != null) {
            sql.append(" and e.order.paidAt<= :paidAtEnd");
            paramMap.put("paidAtEnd", DateUtil.getEndOfDay(paidAtEnd));
        }
        if (QueryType.GOODS_NAME.toString().equals(searchKey) && StringUtils.isNotBlank(searchItems)) {

            sql.append(" and e.goods.shortName like :name");
            paramMap.put("name", "%" + searchItems.trim() + "%");
        }

        if (QueryType.CLERK_JOB_NUMBER.toString().equals(searchKey) && StringUtils.isNotBlank(searchItems)) {
            sql.append(" and e.supplierUser.jobNumber=:jobNumber");
            paramMap.put("jobNumber", searchItems);
        }

        if (QueryType.ORDER_NUMBER.toString().equals(searchKey) && StringUtils.isNotBlank(searchItems)) {
            sql.append(" and e.order.orderNumber like :orderNumber");
            paramMap.put("orderNumber", "%" + searchItems + "%");
        }

        if (QueryType.SHOP_NAME.toString().equals(searchKey) && StringUtils.isNotBlank(searchItems)) {
            sql.append(" and (e.shop.name like :shopLike or e.shop.address like :shopLike)");
            paramMap.put("shopLike", "%" + searchItems + "%");
        }

        if (QueryType.COUPON.toString().equals(searchKey) && StringUtils.isNotBlank(searchItems)) {
            sql.append(" and e.eCouponSn like :eCouponSn");
            paramMap.put("eCouponSn", "%" + searchItems.trim() + "%");
        }


        if (QueryType.MOBILE.toString().equals(searchKey) && StringUtils.isNotBlank(searchItems)) {
            sql.append(" and e.orderItems.phone like :phone");
            paramMap.put("phone", "%" + searchItems.trim() + "%");
        }

        if (QueryType.UID.toString().equals(searchKey) && StringUtils.isNotBlank(searchItems)) {
            sql.append(" and e.order.userId = :uid");
            paramMap.put("uid", Long.parseLong(searchItems));
        }


        //CRM  查询券
        if (StringUtils.isNotBlank(allSearch)) {

            sql.append(" and  e.order.userType = models.accounts.AccountType.CONSUMER");

            sql.append(" and  e.order.orderNumber like :allSearch");
            paramMap.put("allSearch", "%" + allSearch + "%");

            sql.append(" or  e.eCouponSn like :allSearch");
            paramMap.put("allSearch", "%" + allSearch);

            sql.append(" or e.orderItems.phone =:allSearch");
            paramMap.put("allSearch", allSearch);

            sql.append(" or e.id in (select c.couponId from CouponCallBind c where c.phone=:allSearch)");
            paramMap.put("allSearch", allSearch);

            sql.append(" or e.order.buyerMobile = :allSearch");
            paramMap.put("allSearch", allSearch);

            sql.append(" or e.order.receiverMobile=:allSearch");
            paramMap.put("allSearch", allSearch);

            sql.append(" or e.order.userId in (select u.id from User u where e.order.userId = u.id and u.mobile=:allSearch )");
            paramMap.put("allSearch", allSearch);

            sql.append(" or e.order.userId in (select u.id from User u where e.order.userId = u.id and u.loginName=:allSearch )");
            paramMap.put("allSearch", allSearch);


        }


        if (brandId != 0) {
            sql.append(" and e.orderItems.goods.brand =:brand");
            Brand brand = new Brand();
            brand.id = brandId;
            paramMap.put("brand", brand);
        }
        if (StringUtils.isNotBlank(goodsName)) {
            sql.append(" and e.goods.shortName like :name");
            paramMap.put("name", "%" + goodsName.trim() + "%");
        }
        if (status != null) {
            sql.append(" and e.status = :status");
            paramMap.put("status", status);
        }
        if (userId == null) {
            if (isLottery) {
                sql.append(" and e.goods.isLottery = true");
            } else {
                sql.append(" and e.goods.isLottery = false");
            }
            if (accountType != null) {
                sql.append(" and e.order.userType = :userType");
                paramMap.put("userType", accountType);
            }
        }

        if (verifyType != null) {
            sql.append(" and e.verifyType = :verifyType");
            paramMap.put("verifyType", verifyType);
        }

        if (excludeStatus != null) {
            sql.append(" and e.status != :excludeStatus");
            paramMap.put("excludeStatus", excludeStatus);
        }

        if (supplier != null) {
            sql.append(" and e.orderItems.goods.supplierId = :supplierId");
            paramMap.put("supplierId", supplier.id);
        }

        if ((hasSeeAllSupplierPermission != null && supplier != null && supplier.id == 0 && !hasSeeAllSupplierPermission) || (hasSeeAllSupplierPermission != null && supplier == null && !hasSeeAllSupplierPermission)) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                sql.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap.put("supplierIds", supplierIds);
            }
        }

        //按照帐号检索
        if (userName != null) {
            Resaler resaler = Resaler.findOneByLoginName(userName.trim());
            if (resaler != null) {
                sql.append(" and e.order.userId = :user");
                paramMap.put("user", resaler.id);
            }
        }
        return sql.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
