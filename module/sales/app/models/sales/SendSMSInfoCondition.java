package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-7
 * Time: 下午2:43
 * To change this template use File | Settings | File Templates.
 */
public class SendSMSInfoCondition implements Serializable {


    public String eCouponSn;
    public String mobile;
    //    public Date sendAt;
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
//        String orderType = StringUtils.isBlank(orderByType) ? "DESC" : orderByType;
//        return StringUtils.isBlank(orderBy) ? "g.createdAt DESC" : orderBy + " " + orderType;


        return "s.createdAt DESC";
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }


    private static String getOrderBy(int orderById) {
        String orderBy;
        switch (orderById) {
            case 1:
                orderBy = "g.saleCount";
                break;
            case 2:
                orderBy = "g.salePrice";
                break;
            case 3:
                orderBy = "g.discount";
                break;
            case 4:
                orderBy = "g.materialType, g.createdAt"; //电子券优化显示
                break;
            default:
                orderBy = "g.materialType, g.recommend"; //电子券优化显示
                break;
        }
        return orderBy;
    }

        // + ",sendAt:" + sendAt

    @Override
    public String toString() {
        return super.toString() + "[eCouponSn:" + eCouponSn +
                ",mobile:" + mobile +
                ",taskNo:" + taskNo + ",text:" + text + "]";
    }

}
