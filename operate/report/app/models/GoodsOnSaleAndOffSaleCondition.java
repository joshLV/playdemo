package models;

import com.uhuila.common.util.DateUtil;
import models.sales.ChannelGoodsInfoStatus;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-21
 * Time: 下午4:37
 */
public class GoodsOnSaleAndOffSaleCondition {
    public Date createdAtBegin = DateUtil.getBeginOfDay();
    public Date createdAtEnd = DateUtil.getEndOfDay(new Date());
    public String shortName;
    public String interval = "-1d";
    public String code;
    public List<Long> resaleIds;
    private Map<String, Object> paramMap = new HashMap<>();
    public String jobNumber;

    public String filter(Long operateUserId) {
        StringBuilder builder = new StringBuilder(" where 1=1");
        //暂时不加这个条件，以后可能会要
//        List<Supplier> suppliers = Supplier.find("salesId=?", operateUserId).fetch();
//        List<Long> supplierIds = new ArrayList<>();
//        for (Supplier s : suppliers) {
//            supplierIds.add(s.id);
//        }
//        if (supplierIds.size() > 0) {
//            builder.append(" and c.goods.supplierId in (:supplierIds))");
//            paramMap.put("supplierIds", supplierIds);
//        }
        if (StringUtils.isNotBlank(shortName)) {
            builder.append(" and c.goods.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName + "%");
        }
        if (StringUtils.isNotBlank(code)) {
            builder.append(" and c.goods.code = :code");
            paramMap.put("code", code);
        }

        if (StringUtils.isNotBlank(jobNumber)) {
            builder.append(" and c.goods.supplierId in (select s.id from Supplier s where s.salesId in ( " +
                    " select o.id from OperateUser o where o.jobNumber =:jobNumber))");
            paramMap.put("jobNumber", jobNumber);
        }

        if (resaleIds != null) {
            for (Long id : resaleIds) {
                builder.append(" and c.goods in (select g.goods from ChannelGoodsInfo g where g.resaler.id = :resaleId" + id + " and status=:status)");
                paramMap.put("resaleId" + id, id);
                paramMap.put("status", ChannelGoodsInfoStatus.ONSALE);
            }
        }
        return builder.toString();
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

}
