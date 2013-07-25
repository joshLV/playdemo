package models.order;

import models.sales.Goods;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-7-16
 */
public class GiftCardCondition implements Serializable{
    public Goods goods;
    public Boolean sent;
    private Map<String, Object> params = new HashMap<>();

    public String getFilter() {
        StringBuilder filter = new StringBuilder("1=1");
        if (goods != null) {
            filter.append(" and goods = :goods");
            params.put("goods", goods);
        }
        if (sent != null) {
            if (sent) {
                filter.append(" and appliedAt is not null");
            }else {
                filter.append(" and appliedAt is null");
            }
        }
        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
