package models.order;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.consumer.User;
import models.resale.Resaler;
import models.sales.Brand;
import models.sales.MaterialType;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class OrdersCondition {
    public Map<String, Object> paramsMap = new HashMap<>();
    public Date createdAtBegin;
    public Date createdAtEnd;
    public Date paidAtBegin;
    public Date paidAtEnd;
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
    public String outerOrderId;

    //根据订单出库 查看订单时
    public MaterialType materialType;
    public OrderType orderType;
    public Date itemCreatedAt;
    public OrderStatus itemStatus;
    public Long shihuiSupplierId;
    public Long resalerId;

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
            sql.append(" and o.id in (select oi.order.id from o.orderItems oi where oi.goods.supplierId = :supplierId)");
            paramsMap.put("supplierId", supplierId);
        }

        //根据订单出库(实物） 查看订单时
        if (shihuiSupplierId != null) {
            sql.append(" and o.id in (select oi.order.id from OrderItems oi where oi.goods.supplierId=:shihuiSupplierId " +
                    " and oi.goods.materialType=:materialType and oi.order.orderType=:orderType and oi" +
                    ".order.status=:itemStatus and oi.createdAt<=:itemCreatedAt and oi.createdAt >=:realSoldStartDay   " +
                    " )");
            paramsMap.put("shihuiSupplierId", Supplier.getShihui().id);
            paramsMap.put("materialType", materialType);
            paramsMap.put("orderType", orderType);
            paramsMap.put("itemStatus", itemStatus);
            paramsMap.put("itemCreatedAt", DateUtil.getEndOfDay(itemCreatedAt));
            paramsMap.put("realSoldStartDay", DateUtil.stringToDate("2013-04-01 00:00:00", "yyyy-MM-dd HH:mm:ss"));

        }


        if (hasSeeAllSupplierPermission != null && !hasSeeAllSupplierPermission) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                sql.append(" and o.id in (select oi.order.id from o.orderItems oi where oi.goods.supplierId in (:supplierIds))");
                paramsMap.put("supplierIds", supplierIds);
            } else {
                sql.append(" and 1=2");
            }
        }


        if (userType != null && AccountType.CONSUMER == userType) {
            sql.append(" and o.consumerId is not null");
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
        if (status != null) {
            sql.append(" and ( o.status = :status or EXISTS (select 1 from OrderItems r where r.order=o and r.status = :status)) ");
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
        if (brandId != 0 && brandId != -1) {
            sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.brand =:brand)");
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

        //按物流单号检索
        if (QueryType.EXPRESS_NUMBER.toString().equals(searchKey) && StringUtils.isNotEmpty(searchItems)) {
            sql.append(" and o.id in (select o.id from o.orderItems oi where oi.shippingInfo.expressNumber = :expressNumber)");
            paramsMap.put("expressNumber", searchItems);
        }

        if (StringUtils.isNotBlank(outerOrderId)) {
            OuterOrder outerOrder = OuterOrder.find("orderId=?", outerOrderId).first();
            if (outerOrder != null) {
                if (outerOrder.ybqOrder != null) {
                    sql.append(" and o.id=:orderId");
                    paramsMap.put("orderId", outerOrder.ybqOrder.id);
                } else {
                    sql.append(" and 1=0"); //找不到记录
                }
            } else {
                sql.append(" and 1=0"); //找不到记录
            }
        }

        //CRM  查询订单
        if (StringUtils.isNotBlank(allSearch)) {
            sql.append(" and o.orderNumber = :allSearch");
            sql.append(" or o.receiverMobile =:allSearch");
            sql.append(" or o.receiverMobile =:allSearch");
            sql.append(" or o.buyerMobile =:allSearch");
            sql.append(" or  o.id in (select oi.order.id from o.orderItems oi where oi.phone =:allSearch)");
            sql.append(" or o.consumerId in (select u.id from User u where o.userId = u.id " +
                    "and (u.loginName=:allSearch or u.mobile=:allSearch))");
            paramsMap.put("allSearch", allSearch);
        }

        //按照手机检索
        if (QueryType.MOBILE.toString().equals(searchKey) && StringUtils.isNotEmpty(searchItems)) {
            sql.append(" and o.id in (select o.id from o.orderItems oi where oi.phone =:phone)");
            paramsMap.put("phone", searchItems);
        }
        if (resalerId != null) {
            sql.append(" and o.userId = :user");
            paramsMap.put("user", resalerId);
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
            sql.append(" and o.consumerId = :consumerId");
            paramsMap.put("consumerId", user.getId());
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
            sql.append(" and o.userId = :resalerId");
            paramsMap.put("resalerId", resaler.id);
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
