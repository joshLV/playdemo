package models.order;

import com.uhuila.common.constants.DeletedStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.helper.StringUtil;

/**
 * User: wangjia
 * Date: 12-11-21
 * Time: 下午3:25
 */
public class BatchCouponsCondition implements Serializable {
    public String name;
    public String goodsName;
    public Long operatorId;
    private Map<String, Object> params = new HashMap<>();

    public String getFilter() {
        StringBuilder filter = new StringBuilder("operatorId=" + operatorId);
        if (!StringUtil.isBlank(name)) {
            filter.append(" and name like :name");
            params.put("name", "%" + name + "%");
        }
        if (!StringUtil.isBlank(goodsName)) {
            filter.append(" and goodsName like :goodsName");
            params.put("goodsName", "%" + goodsName + "%");
        }


        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
