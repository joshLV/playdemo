package models.accounts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 * Date: 12-5-9
 */
public class WithdrawBillCondition implements Serializable {
    public Account account;
    public WithdrawBillStatus status;

    private Map<String, Object> params = new HashMap<>();
    public String getFilter(){
        StringBuilder filter = new StringBuilder("1=1");
        if(status != null){
            filter.append(" and status = :status");
            params.put("status", status);
        }
        if(account != null){
            filter.append(" and account = :account");
            params.put("account", account);
        }
        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
