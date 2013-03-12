package models.sales;

import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 库存明细查询条件
 * <p/>
 * User: wangjia
 * Date: 13-3-12
 * Time: 下午2:46
 */
public class InventoryStockItemCondition {
    public long supplierId;
    public long skuId;
    public long brandId;
    public Date beginAt;
    public Date endAt;

    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder builder = new StringBuilder();
        builder.append(" i.deleted = :deleted");
        paramMap.put("deleted", com.uhuila.common.constants.DeletedStatus.UN_DELETED);
        if (supplierId != 0) {
            builder.append(" and i.stock.supplier.id = :supplierId");
            paramMap.put("supplierId", supplierId);
        }
        if (brandId != 0) {
            builder.append(" and i.sku.brand.id = :brandId");
            paramMap.put("brandId", brandId);
        }
        if (skuId != 0) {
            builder.append(" and i.sku.id like :skuId");
            paramMap.put("skuId", skuId);
        }
        if (beginAt != null) {
            builder.append(" and i.createdAt >= :beginAt");
            paramMap.put("beginAt", beginAt);
        }
        if (endAt != null) {
            builder.append(" and i.createdAt <= :endAt");
            paramMap.put("endAt", com.uhuila.common.util.DateUtil.getEndOfDay(endAt));
        }

        return builder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
