package models.order;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-11-23
 * Time: 上午10:38
 * To change this template use File | Settings | File Templates.
 */


public class BatchExportCouponsCondition implements Serializable {
    public List<ECoupon> coupons;
    private Map<String, Object> paramMap = new HashMap<>();

    public String getOrderByExpress() {
        return "e.createdAt desc";
    }

    /**
     * 券查询条件
     *
     * @return sql 查询条件
     */
    public String getFilter() {
        StringBuilder sql = new StringBuilder();
        sql.append(" 1=1 ");

        sql.append(" and e in (select e from BatchCoupons b where e in b.coupons)");


        return sql.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}


