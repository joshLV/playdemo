package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品查询条件.
 * <p/>
 * User: sujie
 * Date: 2/29/12
 * Time: 4:25 PM
 */
public class GoodsCondition {
    public long categoryId;
    public String cityId;
    public String districtId;
    public String areaId;
    public long brandId;
    public BigDecimal priceFrom;
    public BigDecimal priceTo;
    public String orderBy;
    public String orderByType;
    public int orderByNum;
    public int orderByTypeNum;

    private Map<String, Object> paramMap = new HashMap<>();

    public GoodsCondition(String condStr) {
        String[] args = condStr.split("-");
        if (args == null || args.length < 9) {
            throw new IllegalArgumentException("GoodsCondition is illegal!");
        }
        categoryId = StringUtils.isBlank(args[0]) ? 0 : Long
                .parseLong(args[0]);
        cityId = StringUtils.isBlank(args[1]) ? "021" : args[1];
        districtId = args[2];
        areaId = args[3];
        if (!StringUtils.isBlank(districtId) && !areaId.contains(districtId)) {
            areaId = " ";
        }
        brandId = StringUtils.isBlank(args[4]) ? 0 : Long
                .parseLong(args[4]);
        priceFrom = StringUtils.isBlank(args[5]) ? new
                BigDecimal(0) : new BigDecimal(args[5]);
        priceTo = StringUtils.isBlank(args[6]) ? new
                BigDecimal(0) : new BigDecimal(args[6]);
        orderByNum = StringUtils.isBlank(args[7]) ? 0 : Integer.parseInt(args[7]);
        orderBy = StringUtils.isBlank(args[7]) ? getOrderBy(0)
                : getOrderBy(Integer.parseInt(args[7]));
        orderByTypeNum = StringUtils.isBlank(args[8]) ? 1 : Integer.parseInt
                (args[8]);
        orderByType = "1".equals(args[8]) ? "DESC" : "ASC";

    }

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder();
        condBuilder.append(" g.deleted = :deleted");
        paramMap.put("deleted", DeletedStatus.UN_DELETED);
        if (StringUtils.isNotBlank(areaId)) {
            condBuilder.append(" and g.id in (select g.id from g.shops s " +
                    "where s.areaId = :areaId)");
            paramMap.put("areaId", areaId);
        } else if (StringUtils.isNotBlank(districtId)) {
            condBuilder.append(" and g.id in (select g.id from g.shops s " +
                    "where s.areaId like :areaId)");
            paramMap.put("areaId", districtId + "%");
        } else if (StringUtils.isNotBlank(cityId)) {
            condBuilder.append(" and g.id in (select g.id from g.shops s " +
                    "where s.areaId like :areaId)");
            paramMap.put("areaId", cityId + "%");
        }
        if (categoryId != 0) {
            condBuilder.append(" and g.id in (select g.id from " +
                    "g.categories c where c.id = :categoryId)");
            paramMap.put("categoryId", categoryId);
        }
        if (brandId != 0) {
            condBuilder.append(" and g.brand = :brand");
            Brand brand = new Brand();
            brand.id = brandId;
            paramMap.put("brand", brand);
        }
        if (priceFrom.compareTo(new BigDecimal(0)) > 0) {
            condBuilder.append(" and g.salePrice >= :priceFrom");
            paramMap.put("priceFrom", priceFrom);
        }
        if (priceTo.compareTo(new BigDecimal(0)) > 0) {
            condBuilder.append(" and g.salePrice <= :priceTo");
            paramMap.put("priceTo", priceTo);
        }
        return condBuilder.toString();
    }

    public String getOrderByExpress() {
        String orderType = StringUtils.isBlank(orderByType)
                ? "ASC" : orderByType;
        String order = StringUtils.isBlank(orderBy) ? "g.createdAt " +
                "DESC" : orderBy + " " + orderType;
        return order;
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
                orderBy = "g.createdAt";
                break;
            default:
                orderBy = "g.createdAt";
                break;
        }
        return orderBy;
    }

    public String getParams() {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
