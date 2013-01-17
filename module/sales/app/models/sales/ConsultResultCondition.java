package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-9-24
 * Time: 下午12:41
 */
public class ConsultResultCondition {
    public Map<String, Object> paramsMap = new HashMap<>();
    public Date createdAtBegin;
    public Date createdAtEnd;
    public String phone;
    public String text;
    public ConsultType consultType;
    public String createdBy;
    public DeletedStatus deleted;

    public String getOrderByExpress() {

        return "c.createdAt DESC";
    }


    /**
     * 券查询条件
     *
     * @return sql 查询条件
     */
    public String getFilter() {
        StringBuilder sql = new StringBuilder();
        sql.append(" deleted= :deleted and text!=null ");
        paramsMap.put("deleted", DeletedStatus.UN_DELETED);

        if (StringUtils.isNotBlank(phone)) {
            sql.append(" and c.phone=:phone");
            paramsMap.put("phone", phone);

        }

        if (consultType!=null) {
            sql.append(" and c.consultType=:consultType");
            paramsMap.put("consultType", consultType);

        }

        if (StringUtils.isNotBlank(text)) {
            sql.append(" and c.text like :text");
            paramsMap.put("text", "%"+text+"%");

        }

        if (createdAtBegin != null) {
            sql.append(" and c.createdAt >= :createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            sql.append(" and c.createdAt <= :createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }

        if (StringUtils.isNotBlank(createdBy)) {
            sql.append(" and c.createdBy like:createdBy");
            paramsMap.put("createdBy", "%" + createdBy + "%");

        }


        return sql.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramsMap;
    }

}
