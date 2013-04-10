package models.sales;

import com.uhuila.common.constants.DeletedStatus;
 import org.apache.commons.lang.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 13-2-27
 * Time: 下午4:26
 */
public class SkuCondition {
    public long supplierId;
    public String code;
    public String name;
    public long brandId;


    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder builder = new StringBuilder();
        builder.append(" s.deleted = :deleted");
        paramMap.put("deleted", DeletedStatus.UN_DELETED);
        if (supplierId != 0) {
            builder.append(" and s.supplier.id = :supplierId");
            paramMap.put("supplierId", supplierId);
        }
        if (brandId != 0) {
            builder.append(" and s.brand.id = :brandId");
            paramMap.put("brandId", brandId);
        }
        if (StringUtils.isNotBlank(code)) {
             builder.append(" and s.code like :code");
            paramMap.put("code", "%" + code.trim() + "%");
        }
         if (StringUtils.isNotBlank(name)) {
             builder.append(" and s.name like :name");
            paramMap.put("name", "%" + name.trim() + "%");
        }
        return builder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
