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
public class WebTrackRefererCondition {
    public String refererLike;
    
    public Boolean isHost;

    public Date begin = DateUtil.getBeginOfDay();
    public Date end = DateUtil.getEndOfDay(new Date());    
    public String orderBy = "w.createdAt";
    public String orderByType = "DESC";
    public String interval = "0d";

    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder("1=1");
        if (begin != null) {
            condBuilder.append(" and w.createdAt >= :Begin");
            paramMap.put("Begin", begin);
        }
        if (end != null) {
            condBuilder.append(" and w.crezatedAt < :End");
            paramMap.put("End", DateUtil.getEndOfDay(end)); 
        }

        if (StringUtils.isNotBlank(refererLike)) {
            if (isHost == null || !isHost.booleanValue()) {
                condBuilder.append(" and w.referer like :refererLike");
            } else {
                condBuilder.append(" and w.refererHost like :refererLike");
            }
            paramMap.put("refererLike", "%" + refererLike + "%");
        }
        Logger.debug("condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }

    public String getSubjectName() {
        if (isHost == null || !isHost.booleanValue()) {
            return "w.referer";
        } else {
            return "w.refererHost";
        }
        
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
