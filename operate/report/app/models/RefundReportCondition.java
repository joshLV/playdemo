package models;

import com.uhuila.common.util.DateUtil;
import models.order.ECouponStatus;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.util.*;

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

    public String getFilter(Long operatorId, Boolean hasSeeAllSupplierPermission) {
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

        if ((StringUtils.isNotBlank(supplierName) && !hasSeeAllSupplierPermission) || (StringUtils.isBlank(supplierName) && !hasSeeAllSupplierPermission)) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and e.goods.supplierId in (:supplierIds)");
                paramMap.put("supplierIds", supplierIds);
            }
        }
        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

}
