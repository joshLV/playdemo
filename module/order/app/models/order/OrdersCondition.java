package models.order;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.consumer.User;
import models.resale.Resaler;
import models.sales.Brand;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OrdersCondition {
    public Map<String, Object> paramsMap = new HashMap<>();
    public Date createdAtBegin;
    public Date createdAtEnd;
    public Date refundAtBegin;
    public Date refundAtEnd;
    public OrderStatus status;
    public String goodsName;
    public DeliveryType deliveryType;
    public String payMethod;
    public String searchKey;
    public String searchItems;
    public boolean isLottery;
    public AccountType userType;
    public long brandId = 0;

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
        if (userType != null) {
            sql.append(" and o.userType=:userType)");
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
            sql.append(" and o.payMethod like :payMethod");
            paramsMap.put("payMethod", "" + payMethod + "%");
        }
        if (brandId != 0) {
            sql.append("and o.id in (select o.id from o.orderItems oi where oi.goods.brand =:brand)");
            Brand brand = new Brand();
            brand.id = brandId;
            paramsMap.put("brand", brand);
        }
        //按照商品名称检索
        if (QueryType.GOODS_NAME.toString().equals(searchKey)) {
            sql.append(" and o.id in (select o.id from o.orderItems oi where oi.goods.name like :name)");
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
            Resaler resaler = Resaler.findByLoginName(searchItems.trim());
            if (resaler != null) {
                sql.append(" and o.userId = :user");
                paramsMap.put("user", resaler.id);
            }
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
            sql.append(" and o.userId = :user and o.userType = :userType");
            paramsMap.put("user", user.getId());
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
        String orderBySql = "o.paidAt desc,o.createdAt desc";
        return orderBySql;
    }

    public String getUserOrderByExpress() {
        String orderBySql = "o.createdAt desc";
        return orderBySql;
    }
}
