package models;

import models.order.ECouponStatus;
import org.apache.commons.lang.StringUtils;

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
    public Date begin = com.uhuila.common.util.DateUtil.getBeginOfDay();
    public Date end = com.uhuila.common.util.DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    public String shortName;
    public String code;
    private Map<String, Object> paramMap = new HashMap<>();
    private Map<String, Object> paramMap1 = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder(" where (r.order.status='PAID' or r.order.status='SENT') and r.goods.isLottery=false");
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.code = :code");
            paramMap.put("code", code);
        }
        if (begin != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", begin);
        }
        if (end != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(end));
        }


        return condBuilder.toString();
    }

    public String getRefundFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.status=:status and e.goods.isLottery=false");
        paramMap1.put("status", ECouponStatus.REFUND);
        if (begin != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap1.put("refundAtBegin", begin);
        }
        if (end != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap1.put("refundAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(end));
        }

        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public Map<String, Object> getParamMap1() {
        return paramMap1;
    }
}
