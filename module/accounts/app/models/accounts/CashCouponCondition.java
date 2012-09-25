package models.accounts;

import com.uhuila.common.constants.DeletedStatus;
import org.jsoup.helper.StringUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-9-25
 */
public class CashCouponCondition implements Serializable {
    public String name;
    public String chargeCode;
    public String serialNo;
    public DeletedStatus deletedStatus;
    public DeletedStatus usedStatus;
    private Map<String, Object> params = new HashMap<>();

    public String getFilter() {
        StringBuilder filter = new StringBuilder("1=1");
        if (!StringUtil.isBlank(name)) {
            filter.append(" and name like :name");
            params.put("name", name + "%");
        }
        if(!StringUtil.isBlank(chargeCode)){
            filter.append(" and chargeCode = :chargeCode");
            params.put("chargeCode", chargeCode);
        }
        if(!StringUtil.isBlank(serialNo)){
            filter.append(" and serialNo like :serialNo");
            params.put("serialNo", serialNo + "%");
        }

        if(deletedStatus != null){
            if(deletedStatus == DeletedStatus.DELETED){
                filter.append(" and deleted = :deletedStatus");
                params.put("deletedStatus", DeletedStatus.DELETED);
            }else {
                filter.append(" and deleted != :deletedStatus");
                params.put("deletedStatus", DeletedStatus.DELETED);
            }
        }

        if(usedStatus != null){
            if(usedStatus == DeletedStatus.DELETED){
                filter.append(" and (userId != null or chargedAt != null)");
            }else {
                filter.append(" and (userId = null or chargedAt = null)");
            }
        }
        return filter.toString();
    }

    public Map<String, Object> getParams(){
        return params;
    }
}
