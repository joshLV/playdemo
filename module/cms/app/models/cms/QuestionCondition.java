package models.cms;

import com.uhuila.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-6-15
 * Time: 下午6:57
 */
public class QuestionCondition implements Serializable {
    
    private static final long serialVersionUID = 812320608652311L;
    
    public Date createdAtBegin;
    public Date createdAtEnd;

    public String content;

    public Map<String, Object> paramsMap = new HashMap<>();

    public String getFitter() {
        StringBuilder sql = new StringBuilder("1=1");
//        sql.append(" and q.visible =:visible");
//        paramsMap.put("visible", true);
        if (createdAtBegin != null) {
            sql.append(" and q.createdAt >= :createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            sql.append(" and q.createdAt <= :createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }
        if (StringUtils.isNotBlank(content)) {
            sql.append(" and q.content like :content");
            paramsMap.put("content", "%" + content + "%");
        }

        return sql.toString();
    }
}
