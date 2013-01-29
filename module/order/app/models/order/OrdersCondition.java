package models.order;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.consumer.User;
import models.resale.Resaler;
import models.sales.Brand;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class OrdersCondition {
    public Map<String, Object> paramsMap = new HashMap<>();
    public Date createdAtBegin;
    public Date createdAtEnd;
    public Date paidAtBegin;
    public Date paidAtEnd;
    public Date refundAtBegin;
    public Date refundAtEnd;
    public OrderStatus status;
    public String goodsName;
    public String orderBy;
    public String orderByType;
    public DeliveryType deliveryType;
    public String payMethod;
    public String searchKey;
    public String searchItems;
    public String allSearch;
    public boolean isLottery;
    public AccountType userType;
    public long brandId = 0;
    public Long operatorId;
    public Boolean hasSeeAllSupplierPermission;
    public Date hidPaidAtBegin;
    public Date hidPaidAtEnd;
    public Long outerOrderId;

    /**
     * 查询条件hql.
     *
     * @param supplierId 商户ID
     * @return sql 查询条件
     */
    public String getFilter(Long supplierId) {
        StringBuilder sql = new StringBuilder();
        sql.append(" o.deleted = :deleted");
        paramsMap.put("deleted", DeletedStatus.UN_DELETED);

        if (supplierId != null) {
            sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.supplierId = :supplierId)");
            paramsMap.put("supplierId", supplierId);
        }


        if (hasSeeAllSupplierPermission != null && !hasSeeAllSupplierPermission) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.supplierId in (:supplierIds))");
                paramsMap.put("supplierIds", supplierIds);
            } else {
                sql.append(" and 1=2");
            }
        }


        if (userType != null) {
            sql.append(" and o.userType=:userType");
            paramsMap.put("userType", userType);
        }
        if (createdAtBegin != null) {
            sql.append(" and o.createdAt >= :createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            sql.append(" and o.createdAt <= :createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        if (paidAtBegin != null) {
            sql.append(" and o.paidAt >= :paidAtBegin");
            paramsMap.put("paidAtBegin", paidAtBegin);
        }
        if (paidAtEnd != null) {
            sql.append(" and o.paidAt <= :paidAtEnd");
            paramsMap.put("paidAtEnd", DateUtil.getEndOfDay(paidAtEnd));
        }
        if (hidPaidAtBegin != null) {
            sql.append(" and o.paidAt >= :paidAtBegin");
            paramsMap.put("paidAtBegin", hidPaidAtBegin);
        }
        if (hidPaidAtEnd != null) {
            sql.append(" and o.paidAt <= :paidAtEnd");
            paramsMap.put("paidAtEnd", DateUtil.getEndOfDay(hidPaidAtEnd));
        }
        if (refundAtBegin != null) {
            sql.append(" and o.refundAt >= :refundAtBegin");
            paramsMap.put("refundAtBegin", refundAtBegin);
        }
        if (refundAtEnd != null) {
            sql.append(" and o.refundAt <= :refundAtEnd");
            paramsMap.put("refundAtEnd", DateUtil.getEndOfDay(refundAtEnd));
        }
        if (status != null) {
            sql.append(" and o.status = :status");
            paramsMap.put("status", status);
        }
        if (isLottery) {
            sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.isLottery = true)");
        } else {
            sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.isLottery = false)");
        }
        if (deliveryType != null) {
            sql.append(" and o.deliveryType = :deliveryType");
            paramsMap.put("deliveryType", deliveryType);
        }
        if (StringUtils.isNotBlank(payMethod)) {
            sql.append(" and o.payMethod = :payMethod");
            paramsMap.put("payMethod", payMethod);
        }
        if (brandId != 0) {
            sql.append("and o.id in (select o.id from o.orderItems oi where oi.goods.brand =:brand)");
            Brand brand = new Brand();
            brand.id = brandId;
            paramsMap.put("brand", brand);
        }
        //按照商品名称检索
        if (QueryType.GOODS_NAME.toString().equals(searchKey)) {
            sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.shortName like :name)");
            paramsMap.put("name", "%" + searchItems.trim() + "%");
        }
        //按照商品订单检索
        if (QueryType.ORDER_NUMBER.toString().equals(searchKey) && StringUtils.isNotEmpty(searchItems)) {
            sql.append(" and o.orderNumber like :orderNumber");
            paramsMap.put("orderNumber", "%" + searchItems + "%");
        }
        //按照帐号检索
        if (QueryType.LOGIN_NAME.toString().equals(searchKey) && StringUtils.isNotEmpty(searchItems)) {
            User user = User.findByLoginName(searchItems.trim());
            if (user != null) {
                sql.append(" and o.userId = :user");
                paramsMap.put("user", user.getId());
            }
            Resaler resaler = Resaler.findOneByLoginName(searchItems.trim());
            if (resaler != null) {
                sql.append(" and o.userId = :user");
                paramsMap.put("user", resaler.id);
            }
        }
        if (outerOrderId != null) {
            OuterOrder outerOrder = OuterOrder.find("orderId=?", outerOrderId).first();
            if (outerOrder != null) {
                sql.append(" and o.id=:orderId");
                paramsMap.put("orderId", outerOrder.ybqOrder.id);
            } else {
                sql.append(" and 1=0"); //找不到记录
            }
        }

        //CRM  查询订单
        if (StringUtils.isNotBlank(allSearch)) {


            sql.append(" and o.userType = models.accounts.AccountType.CONSUMER");


            sql.append(" and o.orderNumber = :allSearch");
            paramsMap.put("allSearch", allSearch);

            sql.append(" or o.receiverMobile =:allSearch");
            paramsMap.put("allSearch", allSearch);

            sql.append(" or o.receiverMobile =:allSearch");
            paramsMap.put("allSearch", allSearch);

            sql.append(" or o.buyerMobile =:allSearch");
            paramsMap.put("allSearch", allSearch);

            sql.append(" or  o.id in (select oi.order.id from o.orderItems oi where oi.phone =:allSearch)");
            paramsMap.put("allSearch", allSearch);

            sql.append(" or o.userId in (select u.id from User u where o.userId = u.id and u.loginName=:allSearch )");
            paramsMap.put("allSearch", allSearch);


            sql.append(" or o.userId in (select u.id from User u where o.userId = u.id and u.mobile=:allSearch )");
            paramsMap.put("allSearch", allSearch);


        }

        //按照手机检索
        if (QueryType.MOBILE.toString().equals(searchKey) && StringUtils.isNotEmpty(searchItems)) {
            sql.append(" and o.id in (select o.id from o.orderItems oi where oi.phone =:phone)");
            paramsMap.put("phone", searchItems);
        }


        return sql.toString();
    }

    /**
     * @param user 用户信息
     * @return sql 查询条件
     */
    public String getFilter(User user) {
        StringBuilder sql = new StringBuilder();
        sql.append(" o.deleted = :deleted");
        paramsMap.put("deleted", DeletedStatus.UN_DELETED);
        if (user != null) {
            sql.append(" and o.userId = :userId and o.userType = :userType");
            paramsMap.put("userId", user.getId());
            paramsMap.put("userType", AccountType.CONSUMER);
        }
        if (createdAtBegin != null) {
            sql.append(" and o.createdAt >= :createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            sql.append(" and o.createdAt <= :createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        if (status != null) {
            sql.append(" and o.status = :status");
            paramsMap.put("status", status);
        }
        //按照订单检索
        if (StringUtils.isNotEmpty(searchItems)) {
            sql.append(" and o.orderNumber like :orderNumber");
            paramsMap.put("orderNumber", "%" + searchItems + "%");
        }
        //按照商品名称检索
        if (StringUtils.isNotBlank(goodsName)) {
            sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.name like :goodsName)");
            paramsMap.put("goodsName", "%" + goodsName + "%");
        }

        return sql.toString();
    }

    /**
     * @param resaler 用户信息
     * @return sql 查询条件
     */
    public String getResalerFilter(Resaler resaler) {
        StringBuilder sql = new StringBuilder();
        sql.append(" o.deleted = :deleted");
        paramsMap.put("deleted", DeletedStatus.UN_DELETED);
        if (resaler != null) {
            sql.append(" and o.userId = :resalerId and o.userType = :userType");
            paramsMap.put("resalerId", resaler.id);
            paramsMap.put("userType", AccountType.RESALER);
        }
        if (createdAtBegin != null) {
            sql.append(" and o.createdAt >= :createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            sql.append(" and o.createdAt <= :createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        if (status != null) {
            sql.append(" and o.status = :status");
            paramsMap.put("status", status);
        }

        //按照商品名称检索
        if (StringUtils.isNotBlank(goodsName)) {
            sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.name like :goodsName)");
            paramsMap.put("goodsName", "%" + goodsName + "%");
        }

        return sql.toString();
    }

    public String getOrderByExpress() {
        String orderBySql = "";
        if (orderBy != null && orderBy != "") {
            orderBySql = orderBySql + orderBy + " ";
        } else {
            orderBySql = orderBySql + "o.paidAt ";
        }

        if (orderByType != null && orderByType != "") {
            orderBySql = orderBySql + orderByType;
        } else {
            orderBySql = orderBySql + "desc";
        }
        //orderBySql = "o.paidAt desc,o.createdAt desc";
        return orderBySql;
    }

    public String getUserOrderByExpress() {
        String orderBySql = "o.createdAt desc";
        return orderBySql;
    }
}
