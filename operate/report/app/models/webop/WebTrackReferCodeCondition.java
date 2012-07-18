package models.webop;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import com.uhuila.common.util.DateUtil;

/**
 * 外链报表查询条件
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 *
 */
public class WebTrackReferCodeCondition {
    public String referCodeLike;
    
    public Date begin = DateUtil.getYesterday();
    public Date end = DateUtil.getEndOfDay(DateUtil.getYesterday());
    public String orderBy = "w.createdAt";
    public String orderByType = "DESC";
    public String interval = "0d";

    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder("w.referCode is not NULL"); 
        if (begin != null) {
            condBuilder.append(" and w.createdAt >= :Begin");
            paramMap.put("Begin", begin);
        }
        if (end != null) {
            condBuilder.append(" and w.createdAt < :End");
            paramMap.put("End", DateUtil.getEndOfDay(end)); 
        }

        if (StringUtils.isNotBlank(referCodeLike)) {
            condBuilder.append(" and w.referCode like :referCodeLike");

            paramMap.put("referCodeLike", "%" + referCodeLike + "%");
        }
        Logger.debug("condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
