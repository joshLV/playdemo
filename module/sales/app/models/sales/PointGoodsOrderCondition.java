package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.accounts.AccountType;
import models.consumer.User;
import models.order.DeliveryType;
import models.order.QueryType;
import models.resale.Resaler;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-8
 * Time: 下午3:30
 * To change this template use File | Settings | File Templates.
 */
public class PointGoodsOrderCondition {
    public Map<String, Object> paramsMap = new HashMap<>();
    public Date applyAtBegin;
    public Date applyAtEnd;
    public Date acceptAtBegin;
    public Date acceptAtEnd;
    public Date refundAtBegin;
    public Date refundAtEnd;
    public PointGoodsOrderStatus status;
    public String pointGoodsName;
    public DeliveryType deliveryType;
    public String searchKey;
    public String searchItems;
    public AccountType userType;

    /**
     * 查询条件hql.
     *
     *
     * @return sql 查询条件
     */
    public String getFilter() {
        StringBuilder sql = new StringBuilder();
        sql.append(" o.deleted = :deleted");
        paramsMap.put("deleted", DeletedStatus.UN_DELETED);

        if (userType != null) {
            sql.append(" and o.userType=:userType");
            paramsMap.put("userType", userType);
        }
        if (applyAtBegin != null) {
            sql.append(" and o.applyAt >= :applyAtBegin");
            paramsMap.put("applyAtBegin", applyAtBegin);
        }
        if (applyAtEnd != null) {
            sql.append(" and o.applyAt <= :applyAtEnd");
            paramsMap.put("applyAtEnd", DateUtil.getEndOfDay(applyAtEnd));
        }
        if (acceptAtBegin != null) {
            sql.append(" and o.acceptAt >= :acceptAtBegin");
            paramsMap.put("acceptAtBegin", acceptAtBegin);
        }
        if (acceptAtEnd != null) {
            sql.append(" and o.acceptAt <= :acceptAtEnd");
            paramsMap.put("acceptAtEnd", DateUtil.getEndOfDay(acceptAtEnd));
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
        if (deliveryType != null) {
            sql.append(" and o.deliveryType = :deliveryType");
            paramsMap.put("deliveryType", deliveryType);
        }

        //按照商品名称检索
        if (QueryType.GOODS_NAME.toString().equals(searchKey)) {
            sql.append(" and o.pointGoodsName like :name");
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
            Resaler resaler = Resaler.findOneLoginName(searchItems.trim());
            if (resaler != null) {
                sql.append(" and o.userId = :user");
                paramsMap.put("user", resaler.id);
            }
        }

        //按照手机检索
        if (QueryType.MOBILE.toString().equals(searchKey) && StringUtils.isNotEmpty(searchItems)) {
            sql.append(" and o.buyerMobile =:phone)");
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
        if (applyAtBegin != null) {
            sql.append(" and o.applyAt >= :applyAtBegin");
            paramsMap.put("applyAtBegin", applyAtBegin);
        }
        if (applyAtEnd != null) {
            sql.append(" and o.applyAt <= :applyAtEnd");
            paramsMap.put("applyAtEnd", DateUtil.getEndOfDay(applyAtEnd));
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
        if (StringUtils.isNotBlank(pointGoodsName)) {
            sql.append(" and o.pointGoods.name like :pointGoodsName");
            paramsMap.put("pointGoodsName", "%" + pointGoodsName + "%");
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
        if (applyAtBegin != null) {
            sql.append(" and o.applyAt >= :applyAtBegin");
            paramsMap.put("applyAtBegin", applyAtBegin);
        }
        if (applyAtEnd != null) {
            sql.append(" and o.applyAt <= :applyAtEnd");
            paramsMap.put("applyAtEnd", DateUtil.getEndOfDay(applyAtEnd));
        }
        if (status != null) {
            sql.append(" and o.status = :status");
            paramsMap.put("status", status);
        }

        //按照商品名称检索
        if (StringUtils.isNotBlank(pointGoodsName)) {
            sql.append(" and o.pointGoods.name like :pointGoodsName)");
            paramsMap.put("pointGoodsName", "%" + pointGoodsName + "%");
        }

        return sql.toString();
    }

    public String getOrderByExpress() {
        String orderBySql = "o.acceptAt desc,o.applyAt desc";
        return orderBySql;
    }

    public String getUserOrderByExpress() {
        String orderBySql = "o.applyAt desc";
        return orderBySql;
    }
}
