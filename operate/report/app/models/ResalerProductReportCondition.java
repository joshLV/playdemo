package models;

import com.uhuila.common.constants.DeletedStatus;
import models.order.OuterOrderPartner;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-21
 * Time: 下午4:37
 */
public class ResalerProductReportCondition {
    public String shortName;
    public String code;
    public List<OuterOrderPartner> partners;
    private Map<String, Object> paramMap = new HashMap<>();

    public String filter() {
        StringBuilder builder = new StringBuilder(" where (c.deleted is null or c.deleted != :deleted)");
        paramMap.put("deleted", DeletedStatus.DELETED);
        if (StringUtils.isNotBlank(shortName)) {
            builder.append(" and c.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            builder.append(" and c.goods.code = :code");
            paramMap.put("code", code);
        }

        if (partners != null && partners.size()>0) {
            builder.append("and c.partner in :partners");
            paramMap.put("partners", partners);
        }
        return builder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

}
