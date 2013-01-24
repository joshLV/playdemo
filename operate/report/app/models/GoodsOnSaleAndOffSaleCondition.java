package models;

import com.uhuila.common.util.DateUtil;
import models.sales.ChannelGoodsInfoStatus;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-21
 * Time: 下午4:37
 */
public class GoodsOnSaleAndOffSaleCondition {
    public Date createdAtBegin = DateUtil.getBeginOfDay();
    public Date createdAtEnd = DateUtil.getEndOfDay(new Date());
    public String shortName;
    public String interval = "-1d";
    public String code;
    public List<Long> resaleIds;
    private Map<String, Object> paramMap = new HashMap<>();

    public String filter() {
        StringBuilder builder = new StringBuilder(" where 1=1 ");
        if (shortName != null) {
            builder.append(" and c.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            builder.append(" and r.goods.code = :code");
            paramMap.put("code", code);
        }
        if (resaleIds != null) {
            builder.append(" and c.goods in (select g.goods from ChannelGoodsInfo g where g.resaler.id in ( :resaleIds) and status=:status)");
            paramMap.put("resaleIds", resaleIds);
            paramMap.put("status", ChannelGoodsInfoStatus.ONSALE);
        }
        return builder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

}
