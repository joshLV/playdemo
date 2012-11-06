package models.report;

import com.uhuila.common.util.DateUtil;
import models.supplier.Supplier;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 报表查询条件.
 * <p/>
 * User: sujie
 * Date: 5/17/12
 * Time: 5:41 PM
 */
public class ReportCondition implements Serializable {
    public String shopLike;
    public String goodsLike;
    public String supplierLike;

    public Supplier supplier;
    public Date createdAtBegin = DateUtil.getYesterday();
    public Date createdAtEnd = DateUtil.getEndOfDay(DateUtil.getYesterday());
    public String orderBy = "r.createdAt";
    public String orderByType = "DESC";
    public String interval = "0d";

    public GoodsLevelPriceName levelPriceName;

    private Map<String, Object> paramMap = new HashMap<>();

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder();
        if (createdAtBegin != null) {
            condBuilder.append(" r.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            condBuilder.append(" and r.createdAt < :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }

        if (supplier != null && supplier.id != 0) {
            condBuilder.append(" and r.supplier = :supplier");
            paramMap.put("supplier", supplier);
//            System.out.println("supplier.id:" + supplier.id);
        }

        if (StringUtils.isNotBlank(goodsLike)) {
//            GoodsCondition goodsCondition = new GoodsCondition();
//            goodsCondition.name = goodsLike;
//            List<Goods> goodsList = Goods.findByCondition(goodsCondition, 1, 1000);
//            String goodsIds = "0";
//            for (Goods goods : goodsList) {
//                goodsIds += "," + goods.id;
//            }

            condBuilder.append(" and r.goods.name like :goodsLike");
            paramMap.put("goodsLike", "%" + goodsLike + "%");
        }

        if (StringUtils.isNotBlank(shopLike)) {
//            Shop shopCondition = new Shop();
//            shopCondition.name = shopLike;
//            List<Shop> shops = Shop.query(shopCondition, 1, 1000);
//            String shopIds = "0";
//            for (Shop shop : shops) {
//                shopIds += "," + shop.id;
//            }

            condBuilder.append(" and r.shop.name like :shopLike");
            paramMap.put("shopLike", "%" + shopLike + "%");
        }
        if (StringUtils.isNotBlank(supplierLike)) {
//            List<Supplier> suppliers = Supplier.findListByFullName(supplierLike);
//            String supplierIds = "0";
//            for (Supplier supplier : suppliers) {
//                supplierIds += "," + suppliers.id;
//            }

            condBuilder.append(" and r.supplier.fullName like :supplierLike");
            paramMap.put("supplierLike", "%" + supplierLike + "%");
        }

//        System.out.println("condBuilder.toString():" + condBuilder.toString());
        return condBuilder.toString();
    }

    public String getOrderByExpress() {
        String orderType = StringUtils.isBlank(orderByType) ? "DESC" : orderByType;
        return StringUtils.isBlank(orderBy) ? "r.createdAt DESC" : orderBy + " " + orderType;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }
}
