package models.order;

import com.uhuila.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 实物退货单查询条件.
 * <p/>
 * User: sujie
 * Date: 4/1/13
 * Time: 11:31 AM
 */
public class RealGoodsReturnEntryCondition implements Serializable {

    /**
     * 实物退货状态.
     */
    public RealGoodsReturnStatus status;

    /**
     * 订单号.
     */
    public String orderNumber;

    /**
     * 货品名称.
     */
    public String goodsName;

    public Long goodsId;

    /**
     * 商户标识.
     */
    public Long supplierId;
    /**
     * 申请时间起始时间.
     */
    public Date createdAtBegin = DateUtils.truncate(DateUtils.addDays(new Date(), -7), Calendar.DATE);

    /**
     * 申请时间结束时间.
     */
    public Date createdAtEnd;
    /**
     * 退货时间起始时间.
     */
    public Date returnedAtBegin;

    /**
     * 退货时间结束时间.
     */
    public Date returnedAtEnd;

    private Map<String, Object> paramsMap = new HashMap<>();

    public RealGoodsReturnEntryCondition(Long supplierId, RealGoodsReturnStatus status) {
        this.supplierId = supplierId;
        this.status = status;
    }

    public String getFilter() {
        StringBuilder sqlCond = new StringBuilder("1=1");
        if (paramsMap == null) {
            paramsMap = new HashMap<>();
        }
        if (status != null) {
            sqlCond.append(" and r.status=:status");
            paramsMap.put("status", status);
        }

        if (StringUtils.isNotBlank(orderNumber)) {
            sqlCond.append(" and r.orderItems.order.orderNumber=:orderNumber");
            paramsMap.put("orderNumber", orderNumber);
        }
        if (goodsId != null && goodsId > 0L) {
            sqlCond.append(" and r.orderItems.goods.id=:goodsId");
            paramsMap.put("goodsId", goodsId);
        }
        if (supplierId != null && supplierId > 0L) {
            sqlCond.append(" and r.orderItems.goods.supplierId=:supplierId");
            paramsMap.put("supplierId", supplierId);
        }
        if (StringUtils.isNotBlank(goodsName)) {
            sqlCond.append(" and r.orderItems.goods.shortName like :goodsName");
            paramsMap.put("goodsName", "%" + goodsName + "%");
        }
        if (returnedAtBegin != null) {
            sqlCond.append(" and r.returnedAt>=:returnedAtBegin");
            paramsMap.put("returnedAtBegin", returnedAtBegin);
        }

        if (returnedAtEnd != null) {
            sqlCond.append(" and r.returnedAt<=:returnedAtEnd");
            paramsMap.put("returnedAtEnd", DateUtil.getEndOfDay(returnedAtEnd));
        }

        if (createdAtBegin != null) {
            sqlCond.append(" and r.createdAt>=:createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }

        if (createdAtEnd != null) {
            sqlCond.append(" and r.createdAt<=:createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        System.out.println(sqlCond.toString()+createdAtBegin);
        return sqlCond.toString();
    }

    public Map<String, Object> getParams() {
        return paramsMap;
    }
}
