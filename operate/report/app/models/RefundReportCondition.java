package models;

import com.uhuila.common.util.DateUtil;
import models.order.ECouponStatus;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-7-18
 * Time: 下午4:51
 */
public class RefundReportCondition {
    public Date refundAtBegin = DateUtil.getBeginOfDay();
    public Date refundAtEnd = DateUtil.getEndOfDay(new Date());
    public String interval = "-1d";
    public String goodsName;
    public String supplierName;
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder(" where e.status=:status and e.goods.isLottery=false");
        paramMap.put("status", ECouponStatus.REFUND);
        if (refundAtBegin != null) {
            condBuilder.append(" and e.refundAt >= :refundAtBegin");
            paramMap.put("refundAtBegin", refundAtBegin);
        }
        if (refundAtEnd != null) {
            condBuilder.append(" and e.refundAt <= :refundAtEnd");
            paramMap.put("refundAtEnd", DateUtil.getEndOfDay(refundAtEnd));
        }
        if (StringUtils.isNotBlank(goodsName)) {
            condBuilder.append(" and e.goods.name like :name");
            paramMap.put("name", "%" + goodsName + "%");
        }

        if (StringUtils.isNotBlank(supplierName)) {
            condBuilder.append(" and e.orderItems.goods.supplierId = :supplierId");
            paramMap.put("supplierId", Long.parseLong(supplierName));
        }
        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

}
