package models;

import models.order.ECouponStatus;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 销售报表查询条件
 * <p/>
 * User: wangjia
 * Date: 12-12-11
 * Time: 下午4:07
 */
public class SalesReportCondition implements Serializable {

    public Date begin = com.uhuila.common.util.DateUtil.getYesterday();
    public Date end = com.uhuila.common.util.DateUtil.getYesterday();
    public String interval = "-1d";
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.status=:status and e.goods.isLottery=false");
        paramMap.put("status", ECouponStatus.REFUND);
//        if (refundAtBegin != null) {
//            condBuilder.append(" and e.refundAt >= :refundAtBegin");
//            paramMap.put("refundAtBegin", refundAtBegin);
//        }

        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
