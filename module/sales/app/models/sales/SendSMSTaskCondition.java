package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-9-13
 * Time: 上午10:25
 */
public class SendSMSTaskCondition   implements Serializable {
    public String taskNo;

    private Map<String, Object> paramMap = new HashMap<>();

    public SendSMSTaskCondition() {

    }

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder();
        condBuilder.append(" st.deleted = :deleted");
        paramMap.put("deleted", DeletedStatus.UN_DELETED);

        if (!StringUtils.isBlank(taskNo)) {
            condBuilder.append(" and st.taskNo = :taskNo");
            paramMap.put("taskNo", taskNo);
        }

        return condBuilder.toString();
    }


    public String getOrderByExpress() {

        return "st.createdAt DESC";
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    @Override
    public String toString() {
        return super.toString() + "[taskNo:" + taskNo +"]";
    }


}
