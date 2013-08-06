package models.supplier;

import com.uhuila.common.constants.DeletedStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * User: yan
 * Date: 13-8-5
 * Time: 下午6:40
 */
public class SupplierAdsFeesCondition {
    public long supplierId;
    public ReceivedType type;

    private Map<String, Object> paramMap = new HashMap<>();

    public String getFitter() {
        StringBuilder builder = new StringBuilder();
        builder.append(" s.deleted = :deleted");
        paramMap.put("deleted", DeletedStatus.UN_DELETED);
        if (supplierId != 0) {
            builder.append(" and s.supplier.id = :supplierId");
            paramMap.put("supplierId", supplierId);
        }
        if (type != null) {
            builder.append(" and s.receivedType = :receivedType");
            paramMap.put("receivedType", type);
        }
        return builder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
