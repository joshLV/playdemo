package models.sales;

import java.util.HashMap;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-15
 * Time: 上午11:23
 */
public class SecKillGoodsCondition {
    public String goodsName;
    private Map<String, Object> paramMap = new HashMap<>();

    public SecKillGoodsCondition() {

    }

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder("1=1");
        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }


}
