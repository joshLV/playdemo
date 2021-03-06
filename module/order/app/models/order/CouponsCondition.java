package models.order;

import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.resale.Resaler;
import models.sales.Brand;
import models.supplier.Supplier;
import models.supplier.SupplierCategory;
import org.apache.commons.lang.StringUtils;
import play.Logger;

import java.io.Serializable;
import java.util.*;

public class CouponsCondition implements Serializable {

//    private static final long serialVersionUID = 1632320311301L;

    public Date createdAtBegin;
    public Date createdAtEnd;

    public Date consumedAtBegin;
    public Date consumedAtEnd;

    public Date virtualVerifyAtBegin;
    public Date virtualVerifyAtEnd;
    public boolean virtualVerify;

    public Date refundAtBegin;
    public Date refundAtEnd;

    public Date cheatedAtBegin;
    public Date cheatedAtEnd;

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
    public Long shopId;
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
    public Date hidPaidAtBegin;
    public Date hidPaidAtEnd;
    public String userName;
    public Long operatorId;
    public Boolean hasSeeAllSupplierPermission;
    public Long goodsId;
    public String supplierCategoryCode;
    public String refundForm = "";

    public Boolean isCheatedOrder;

    public Boolean isOrder;//商品是否需要预约

    public Long salesId;
    public String categoryCode;
    public Date expiredAtBegin = DateUtil.getBeginExpiredDate(3);
    public Date expiredAtEnd = DateUtil.getEndExpiredDate(3);

    public String partnerCouponId;

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
            if (accountType == AccountType.CONSUMER) {
                sql.append(" and e.order.consumerId = :consumerId");
                paramMap.put("consumerId", userId);
            } else {
                sql.append(" and e.order.userId = :userId");
                paramMap.put("userId", userId);
            }
        }


        if (createdAtBegin != null) {
            sql.append(" and e.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", createdAtBegin);
        }


        if (createdAtEnd != null) {
            sql.append(" and e.createdAt <= :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }

        if (cheatedAtBegin != null) {
            sql.append(" and e.cheatedAt >= :cheatedAtBegin");
            paramMap.put("cheatedAtBegin", cheatedAtBegin);
        }


        if (cheatedAtEnd != null) {
            sql.append(" and e.cheatedAt <= :cheatedAtEnd");
            paramMap.put("cheatedAtEnd", DateUtil.getEndOfDay(cheatedAtEnd));
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
            //本期购买，本期未消费退款
            if (refundForm.equals("salesRefund")) {
                sql.append(" and e.consumedAt is null ");
                sql.append(" and e.createdAt >= :createdAtBegin");
                paramMap.put("createdAtBegin", refundAtBegin);
                //本期之前购买，本期未消费退款
            } else if (refundForm.equals("previousSalesRefund")) {
                sql.append(" and e.consumedAt is null ");
                sql.append(" and e.createdAt < :createdAtBegin");
                paramMap.put("createdAtBegin", refundAtBegin);
            }
            //本期消费，本期消费退款
            else if (refundForm.equals("consumedRefund")) {
                sql.append(" and e.consumedAt is not null  ");
                sql.append(" and e.consumedAt >= :createdAtBegin");
                paramMap.put("createdAtBegin", refundAtBegin);
                //本期之前消费，本期消费退款
            } else if (refundForm.equals("previousConsumedRefund")) {
                sql.append(" and e.consumedAt is not null ");
                sql.append(" and e.consumedAt < :createdAtBegin");
                paramMap.put("createdAtBegin", refundAtBegin);
            }
        }

        if (refundAtEnd != null) {
            sql.append(" and e.refundAt <= :refundAtEnd");
            paramMap.put("refundAtEnd", DateUtil.getEndOfDay(refundAtEnd));
            //本期购买，本期未消费退款
            if (refundForm.equals("salesRefund")) {
                sql.append(" and e.consumedAt is null ");
                sql.append(" and e.createdAt <= :createdAtEnd");
                paramMap.put("createdAtEnd", DateUtil.getEndOfDay(refundAtEnd));
            } //本期消费，本期消费退款
            else if (refundForm.equals("consumedRefund")) {
                sql.append(" and e.consumedAt is not null  ");
                sql.append(" and e.consumedAt <= :createdAtEnd");
                paramMap.put("createdAtEnd", DateUtil.getEndOfDay(refundAtEnd));
                //本期之前消费，本期消费退款
            }

        }
        if (virtualVerifyAtBegin != null) {
            sql.append(" and e.virtualVerifyAt >= :virtualVerifyAtBegin");
            paramMap.put("virtualVerifyAtBegin", virtualVerifyAtBegin);
        }

        if (virtualVerifyAtEnd != null) {
            sql.append(" and e.virtualVerifyAt <= :virtualVerifyAtEnd");
            paramMap.put("virtualVerifyAtEnd", DateUtil.getEndOfDay(virtualVerifyAtEnd));
        }
        if (virtualVerify) {
            sql.append(" and e.virtualVerify =true");
        }
        if (paidAtBegin != null) {
            sql.append(" and e.order.paidAt>= :paidAtBegin");
            paramMap.put("paidAtBegin", paidAtBegin);
        }

        if (paidAtEnd != null) {
            sql.append(" and e.order.paidAt<= :paidAtEnd");
            paramMap.put("paidAtEnd", DateUtil.getEndOfDay(paidAtEnd));
        }

        if (hidPaidAtBegin != null) {
            sql.append(" and e.order.paidAt>= :paidAtBegin");
            paramMap.put("paidAtBegin", hidPaidAtBegin);
        }

        if (hidPaidAtEnd != null) {
            sql.append(" and e.order.paidAt<= :paidAtEnd");
            paramMap.put("paidAtEnd", DateUtil.getEndOfDay(hidPaidAtEnd));
        }

        if (shopId != null) {
            sql.append(" and e.shop.id = :shopId");
            paramMap.put("shopId", shopId);
        }
        if (isOrder != null && isOrder) {
            sql.append(" and e.goods.isOrder = :isOrder and e.appointmentDate is null");
            paramMap.put("isOrder", isOrder);
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
        if (StringUtils.isNotBlank(eCouponSn)) {
            sql.append(" and e.eCouponSn like :eCouponSn");
            paramMap.put("eCouponSn", "%" + eCouponSn + "%");
        }
        if (brandId != null && brandId != 0) {
            sql.append(" and e.orderItems.goods.brand =:brand");
            Brand brand = new Brand();
            brand.id = brandId;
            paramMap.put("brand", brand);
        }
        if (StringUtils.isNotBlank(goodsName)) {
            sql.append(" and e.goods.name like :name");
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

        if (StringUtils.isNotBlank(supplierCategoryCode)) {
            SupplierCategory supplierCategory = SupplierCategory.find("byCode", supplierCategoryCode).first();
            List<Supplier> supplierList = Supplier.find("bySupplierCategory", supplierCategory).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : supplierList) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                sql.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap.put("supplierIds", supplierIds);
            } else {
                sql.append(" and 5 =:supplierIds");
                paramMap.put("supplierIds", 6);
            }
        }

        if (hasSeeAllSupplierPermission != null && !hasSeeAllSupplierPermission) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                sql.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap.put("supplierIds", supplierIds);
            } else {
                sql.append(" and 5 =:supplierIds");
                paramMap.put("supplierIds", 6);
            }
        }

        if (StringUtils.isNotBlank(partnerCouponId)) {
            sql.append(" and e.partnerCouponId = :partnerCouponId");
            paramMap.put("partnerCouponId", partnerCouponId);
        }

        //按照帐号检索
        if (userName != null) {
            Resaler resaler = Resaler.findOneByLoginName(userName.trim());
            if (resaler != null) {
                sql.append(" and e.order.userId = :user");
                paramMap.put("user", resaler.id);
            }
        }

        if (goodsId != null) {
            sql.append(" and e.goods.id = :goodsId");
            paramMap.put("goodsId", goodsId);
        }
        List<Long> supplierIds = new ArrayList<>();

        if (salesId != null) {
            List<Supplier> suppliers = null;
            StringBuilder sq = new StringBuilder(" salesId= ? ");
            List list = new ArrayList();
            list.add(salesId);
            SupplierCategory category = null;
            if (StringUtils.isNotBlank(categoryCode)) {
                category = SupplierCategory.find("code=?", categoryCode).first();
            }
            if (category != null) {
                sq.append("and supplierCategory=?");
                list.add(category);
            }

            suppliers = Supplier.find(sq.toString(), list.toArray()).fetch();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }

            if (supplierIds != null && supplierIds.size() > 0) {
                sql.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap.put("supplierIds", supplierIds);
            } else {
                sql.append(" and 1=0");
            }
        }

        if (isCheatedOrder != null && isCheatedOrder) {
            sql.append(" and e.isCheatedOrder=true");
        }

        Logger.info("sql.toString:" + sql.toString());
        return sql.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public String getQueryFitter() {
        StringBuilder sql = new StringBuilder(" where e.virtualVerify=0 and e.goods.isLottery=false and e.status =:status and (e.expireAt >= :expireBeginAt and e.expireAt <= " +
                ":expireEndAt) and e.partner in (:partner) and (e.goods.noRefund = true or e.isCheatedOrder = true )");
        paramMap.put("status", ECouponStatus.UNCONSUMED);
        paramMap.put("expireBeginAt", expiredAtBegin);
        paramMap.put("expireEndAt", DateUtil.getEndOfDay(expiredAtEnd));
        List<ECouponPartner> partnerList = new ArrayList<>();

        partnerList.add(ECouponPartner.JD);
        partnerList.add(ECouponPartner.WB);
        partnerList.add(ECouponPartner.TB);
        paramMap.put("partner", partnerList);

        if (StringUtils.isNotBlank(eCouponSn)) {
            sql.append(" and e.eCouponSn like :eCouponSn");
            paramMap.put("eCouponSn", "%" + eCouponSn + "%");
        }
        if (StringUtils.isNotBlank(goodsName)) {
            sql.append(" and e.goods.name like :name");
            paramMap.put("name", "%" + goodsName.trim() + "%");
        }
        if (StringUtils.isNotBlank(phone)) {
            sql.append(" and e.orderItems.phone like :phone");
            paramMap.put("phone", "%" + phone.trim() + "%");
        }
        return sql.toString();
    }


}
