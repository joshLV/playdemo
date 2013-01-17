package models.consumer;

import models.sales.ConsultType;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * User: wangjia
 * Date: 12-8-31
 * Time: 上午10:59
 */
public class CRMCondition {
    public String loginName;
    public String mobile;
    public String orderNumber;
    public String eCouponSn;
    public String searchUser;
    public String searchOrderCoupon;
    public ConsultType status;
    public String phone;
    public Long userId;

    public List params = new ArrayList();

    public String getFilter() {

        StringBuilder sq = new StringBuilder("1=1");
        if (StringUtils.isNotBlank(loginName)) {
            User user = User.findByLoginName(loginName.trim());
            if (user != null) {
                sq.append(" and order.userId = :user");
                params.add(user.getId());
            }
        }
        if (StringUtils.isNotBlank(mobile)) {
            User user = User.findByLoginName(mobile.trim());
            if (user != null) {
                sq.append(" and order.userId = :user");
                params.add(user.getId());
            }
        }

        if (StringUtils.isNotBlank(orderNumber)) {
            sq.append(" and Order.orderNumber = :orderNumber");
            params.add(orderNumber);
        }

        if (StringUtils.isNotBlank(eCouponSn)) {
            sq.append(" and eCouponSn = :eCouponSn");
            params.add(" like '%" + eCouponSn + "%'");
        }


        return sq.toString();
    }

    public List<String> getParams() {
        return params;
    }


}

