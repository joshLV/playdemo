package models.supplier;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-11-29
 * Time: 下午2:06
 */
public class SupplierCategoryCondition implements Serializable {
    public Map<String, Object> paramsMap = new HashMap<>();


    public String code;

    public String name;

    public String orderByType = "DESC";

    /**
     * 查询条件hql.
     *
     * @param supplierId 商户ID
     * @return sql 查询条件
     */
    public String getFilter() {
        StringBuilder sql = new StringBuilder();
        sql.append(" 1=1 ");
        if (StringUtils.isNotBlank(code)) {
            sql.append("and sc.code = :code");
            paramsMap.put("code", code);
        }
        if (StringUtils.isNotBlank(name)) {
            sql.append("and sc.name = :name");
            paramsMap.put("name", name);
        }
        return sql.toString();
    }

    public String getOrderByExpress() {
        return "sc.createdAt DESC";
    }

    public Map<String, Object> getParamMap() {
        return paramsMap;
    }


    @Override
    public String toString() {
        return super.toString() + "[code:" + code + "," +
                "name:" + name +
                "]";
    }

}

