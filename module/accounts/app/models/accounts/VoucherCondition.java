package models.accounts;

import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-12-12
 */
public class VoucherCondition implements Serializable {
    public String name;
    public Long uid;
    public DeletedStatus deletedStatus;
    public DeletedStatus assignedStatus;
    public DeletedStatus usedStatus;
    public VoucherType voucherType;
    private Map<String, Object> params = new HashMap<>();

    public String getFilter() {
        StringBuilder filter = new StringBuilder("1=1");
        if (!StringUtils.isBlank(name)) {
            filter.append(" and name like :name");
            params.put("name", name + "%");
        }

        if (uid != null) {
            filter.append(" and account.uid = :uid and account.accountType = :accountType");
            params.put("uid", uid);
            params.put("accountType", AccountType.CONSUMER);
        }
        if (voucherType != null) {
            filter.append(" and voucherType =:voucherType");
            params.put("voucherType", voucherType);
        }
        if (deletedStatus != null) {
            if (deletedStatus == DeletedStatus.DELETED) {
                filter.append(" and deleted = :deletedStatus");
            } else {
                filter.append(" and deleted != :deletedStatus");
            }
            params.put("deletedStatus", DeletedStatus.DELETED);
        }
        if (assignedStatus != null) {
            if (deletedStatus == DeletedStatus.DELETED) {
                filter.append(" and account != null");
            } else {
                filter.append(" and account = null");
            }
        }

        if (usedStatus != null) {
            if (usedStatus == DeletedStatus.DELETED) {
                filter.append(" and usedAt != null");
            } else {
                filter.append(" and usedAt = null");
            }
        }

        return filter.toString();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
