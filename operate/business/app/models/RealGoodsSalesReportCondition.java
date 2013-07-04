package models;

import com.uhuila.common.util.DateUtil;
import models.order.ECouponStatus;
import models.order.OrderStatus;
import models.sales.MaterialType;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import java.util.*;

/**
 * User: yan
 * Date: 13-7-3
 * Time: 下午5:02
 */
public class RealGoodsSalesReportCondition {
    public Date beginAt = DateUtils.addDays(new Date(), -1);
    public Date endAt = DateUtils.truncate(new Date(), Calendar.DATE);
    public String interval = "-1d";
    public String shortName;
    public String code;
    public String userName;
    public Long supplierId = 0l;
    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder(" where r.goods.materialType=:materialType and r.goods.isLottery=false" +
                " and r.order.deleted = com.uhuila.common.constants.DeletedStatus.UN_DELETED and r.status <> :unpaidStatus and r.status <>:canceledStatus");
        paramMap.put("materialType", MaterialType.REAL);
        paramMap.put("unpaidStatus", OrderStatus.UNPAID);
        paramMap.put("canceledStatus", OrderStatus.CANCELED);

        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code like :code");
            paramMap.put("code", code.trim() + "%");
        }

        if (beginAt != null) {
            condBuilder.append(" and r.order.paidAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and r.order.paidAt < :createdAtEnd");
            paramMap.put("createdAtEnd", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }

        if (supplierId != 0) {
            condBuilder.append(" and r.goods.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }

        return condBuilder.toString();
    }

    public String getRefundFilter() {
        paramMap = new HashMap<>();
        StringBuilder condBuilder = new StringBuilder(" where rr.orderItems=r and r.status=:status and r.goods.isLottery=false");
        paramMap.put("status", OrderStatus.RETURNED);
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and r.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and r.goods.code like :code");
            paramMap.put("code", code.trim() + "%");
        }
        if (beginAt != null) {
            condBuilder.append(" and rr.returnedAt >= :refundAtBegin");
            paramMap.put("refundAtBegin", beginAt);
        }
        if (endAt != null) {
            condBuilder.append(" and rr.returnedAt <= :refundAtEnd");
            paramMap.put("refundAtEnd", DateUtil.getEndOfDay(endAt));
        }

        return condBuilder.toString();

    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

}
