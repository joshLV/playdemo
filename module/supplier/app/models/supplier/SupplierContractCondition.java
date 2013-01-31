package models.supplier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.lang.StringUtils;


/**
 * 商户合同查询条件
 * <p/>
 * User: wangjia
 * Date: 13-1-30
 * Time: 上午10:11
 */
public class SupplierContractCondition implements Serializable {
    public long supplierId = 0;
    public String orderByType = "DESC";
    public String orderBy = getOrderBy(0);
    public Boolean hasManagerViewContractPermission;
    public Long operatorId;

    private Map<String, Object> paramMap = new HashMap<>();

    public SupplierContractCondition() {

    }

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder();
        condBuilder.append(" c.deleted = :deleted ");
        paramMap.put("deleted", com.uhuila.common.constants.DeletedStatus.UN_DELETED);
        if (supplierId != 0) {
            condBuilder.append(" and c.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }
        if (hasManagerViewContractPermission != null && !hasManagerViewContractPermission) {
            List<Supplier> suppliers = Supplier.find("salesId=?", operatorId).fetch();
            List<Long> supplierIds = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierIds.add(s.id);
            }
            if (supplierIds != null && supplierIds.size() > 0) {
                condBuilder.append(" and c.supplierId in (:supplierIds)");
                paramMap.put("supplierIds", supplierIds);
            } else {
                condBuilder.append(" and 1=0");
            }
        }

        return condBuilder.toString();
    }

    public String getOrderByExpress() {
        String orderType = StringUtils.isBlank(orderByType) ? "DESC" : orderByType;
        return StringUtils.isBlank(orderBy) ? "g.createdAt DESC" : orderBy + " " + orderType;
    }

    private static String getOrderBy(int orderById) {
        String orderBy;
        switch (orderById) {
            case 1:
                orderBy = "c.supplierName";
                break;
            case 2:
                orderBy = "c.companyName";
                break;
            case 3:
                orderBy = "c.effectiveAt";
                break;
            case 4:
                orderBy = "c.expireAt";
                break;
            case 5:
                orderBy = "c.createdAt";
                break;
            default:
                orderBy = "c.updatedAt";
                break;
        }
        return orderBy;

    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }


}