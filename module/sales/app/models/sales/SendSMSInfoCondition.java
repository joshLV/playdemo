package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: wangjia
 * Date: 12-9-7
 * Time: 下午2:43
 */
public class SendSMSInfoCondition implements Serializable {

    public String eCouponSn;
    public String mobile;
    //  public Date sendAt;
    public String taskNo;
    public String text;

    private Map<String, Object> paramMap = new HashMap<>();

    public SendSMSInfoCondition() {

    }


    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder();
        condBuilder.append(" s.deleted = :deleted");
        paramMap.put("deleted", DeletedStatus.UN_DELETED);

        if (!StringUtils.isBlank(taskNo)) {
            condBuilder.append(" and s.taskNo = :taskNo");
            paramMap.put("taskNo", taskNo);
        }

        return condBuilder.toString();
    }

       public String getOrderByExpress() {

        return "s.createdAt DESC";
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }


        // + ",sendAt:" + sendAt

    @Override
    public String toString() {
        return super.toString() + "[eCouponSn:" + eCouponSn +
                ",mobile:" + mobile +
                ",taskNo:" + taskNo + ",text:" + text + "]";
    }

}
