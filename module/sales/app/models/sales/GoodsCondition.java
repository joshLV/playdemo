package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.resale.Resaler;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品查询条件.
 * <p/>
 * User: sujie
 * Date: 2/29/12
 * Time: 4:25 PM
 */
public class GoodsCondition implements Serializable {

    private static final long serialVersionUID = 73232320609113062L;

    public static final String SHANGHAI = "021";

    public long supplierId = 0;
    public long parentCategoryId = 0;
    public long categoryId = 0;
    public String cityId = SHANGHAI;
    public String districtId = "0";
    public String areaId = "0";
    public String searchAreaId = "0";
    public long brandId = 0;
    public BigDecimal priceFrom = BigDecimal.ZERO;
    public BigDecimal priceTo = BigDecimal.ZERO;
    public String orderBy = getOrderBy(0);
    public String solrOrderBy = getSolrOrderBy(0);
    public String orderByType = "DESC";
    public int orderByNum = 9;
    public int orderByTypeNum = 0;
    public int type = 0;
    public String name;
    public String shortName;
    public String no;
    public String code;
    public String jobNumber;


    public BigDecimal pointPriceBegin;
    public BigDecimal pointPriceEnd;


    public BigDecimal salePriceBegin;
    public BigDecimal salePriceEnd;
    public Integer saleCountBegin;
    public Integer saleCountEnd;
    public MaterialType materialType;
    public GoodsStatus status;
    public long baseSaleBegin = -1;
    public long baseSaleEnd = -1;
    public int priority;
    public boolean isLottery;
    public boolean isHideOnsale;
    public Date expireAtBegin;
    public Date expireAtEnd;
    public Date expireAt;

    private Map<String, Object> paramMap = new HashMap<>();

    public GoodsCondition() {

    }

    /**
     * 拼接hql的查询条件.
     *
     * @param condStr hql的查询条件
     */
    public GoodsCondition(String condStr) {
        String[] args = condStr.split("-");
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("GoodsCondition is illegal!");
        }

        if (args.length > 0) {
            categoryId = StringUtils.isBlank(args[0]) ? 0 : Long
                    .parseLong(args[0]);
        }
        if (args.length > 1) {
            searchAreaId = StringUtils.isBlank(args[1]) ? SHANGHAI : args[1];
            if (searchAreaId.length() == 8) {
                districtId = searchAreaId.substring(0, 5);
                areaId = searchAreaId;
            } else if (searchAreaId.length() == 5) {
                districtId = searchAreaId;
                areaId = "0";
            }
        }
        if (args.length > 2) {
            brandId = StringUtils.isBlank(args[2]) ? 0 : Long
                    .parseLong(args[2]);
        }
        if (args.length > 3) {
            priceFrom = StringUtils.isBlank(args[3]) ? new
                    BigDecimal(0) : new BigDecimal(args[3]);
        }
        if (args.length > 4) {
            priceTo = StringUtils.isBlank(args[4]) ? new
                    BigDecimal(0) : new BigDecimal(args[4]);
        }
        if (args.length > 5) {
            orderByNum = StringUtils.isBlank(args[5]) ? 0 : Integer.parseInt(args[5]);
            orderBy = StringUtils.isBlank(args[5]) ? getOrderBy(0)
                    : getOrderBy(Integer.parseInt(args[5]));
            solrOrderBy = StringUtils.isBlank(args[5]) ? getSolrOrderBy(0)
                    : getSolrOrderBy(Integer.parseInt(args[5]));
        }
        if (args.length > 6) {
            orderByTypeNum = StringUtils.isBlank(args[6]) ? 1 : Integer.parseInt
                    (args[6]);
            orderByType = "1".equals(args[6]) ? "DESC" : "ASC";
        }
        if (args.length > 7) {
            materialType = StringUtils.isBlank(args[7]) ? MaterialType.ELECTRONIC :
                    MaterialType.values()[Integer.parseInt(args[7])];
        }
    }

    public String getFilter() {
        StringBuilder condBuilder = new StringBuilder();
        condBuilder.append(" g.deleted = :deleted and g.status != :notMatchStatus ");
        paramMap.put("deleted", DeletedStatus.UN_DELETED);
        paramMap.put("notMatchStatus", GoodsStatus.UNCREATED);
        if (isValidAreaId(searchAreaId)) {
            condBuilder.append(" and ((g.isAllShop = true and g.supplierId in (select g.supplierId from Shop gs " +
                    "where gs.supplierId = g.supplierId and gs.areaId like :areaId)) or ( g.isAllShop = false and" +
                    " g.id in (select g.id from g.shops s where s.areaId like :areaId)))");
            paramMap.put("areaId", searchAreaId + "%");
        }

        if (StringUtils.isNotBlank(jobNumber)) {
            condBuilder.append(" and g.supplierId in (select s.id from Supplier s where s.salesId in ( " +
                    " select o.id from OperateUser o where o.jobNumber =:jobNumber))");
            paramMap.put("jobNumber", jobNumber);
        }


        if (supplierId != 0) {
            condBuilder.append(" and g.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }
        if (categoryId != 0) {
            condBuilder.append(" and g.id in (select g.id from " +
                    "g.categories c where c.id = :categoryId or (c.parentCategory is not null and c.parentCategory.id=:categoryId))");
            paramMap.put("categoryId", categoryId);
        }

        if (brandId != 0) {
            condBuilder.append(" and g.brand = :brand");
            Brand brand = new Brand();
            brand.id = brandId;
            paramMap.put("brand", brand);
        }
        if (priceFrom.compareTo(BigDecimal.ZERO) > 0) {
            condBuilder.append(" and g.salePrice >= :priceFrom");
            paramMap.put("priceFrom", priceFrom);
        }
        if (priceTo.compareTo(BigDecimal.ZERO) > 0) {
            condBuilder.append(" and g.salePrice <= :priceTo");
            paramMap.put("priceTo", priceTo);
        }

        if (StringUtils.isNotBlank(name)) {
            condBuilder.append(" and g.name like :name");
            paramMap.put("name", "%" + name.trim() + "%");
        }
        if (StringUtils.isNotBlank(shortName)) {
            condBuilder.append(" and g.shortName like :shortName");
            paramMap.put("shortName", "%" + shortName.trim() + "%");
        }
        if (StringUtils.isNotBlank(no)) {
            condBuilder.append(" and g.no like :no ");
            paramMap.put("no", "%" + no.trim() + "%");
        }

        if (StringUtils.isNotBlank(code)) {
            condBuilder.append(" and g.code like :code ");
            paramMap.put("code", code.trim() + "%");
        }

        if (status != null) {
            condBuilder.append(" and g.status = :status ");
            paramMap.put("status", status);
            if (status.equals(GoodsStatus.ONSALE)) {
                expireAtBegin = new Date();
                baseSaleBegin = 0;
            }
        }


        if (pointPriceBegin != null) {
            condBuilder.append(" and g.pointPrice >= :pointPriceBegin");
            paramMap.put("pointPriceBegin", pointPriceBegin);
        }

        if (pointPriceEnd != null) {
            condBuilder.append(" and g.pointPrice <= :pointPriceEnd");
            paramMap.put("pointPriceEnd", pointPriceEnd);
        }


        if (salePriceBegin != null) {
            condBuilder.append(" and g.salePrice >= :salePriceBegin");
            paramMap.put("salePriceBegin", salePriceBegin);
        }

        if (salePriceEnd != null) {
            condBuilder.append(" and g.salePrice <= :salePriceEnd");
            paramMap.put("salePriceEnd", salePriceEnd);
        }

        if (saleCountBegin != null && saleCountBegin >= 0) {
            condBuilder.append(" and g.saleCount >= :saleCountBegin");
            paramMap.put("saleCountBegin", saleCountBegin);
        }

        if (saleCountEnd != null && saleCountEnd >= 0) {
            condBuilder.append(" and g.saleCount <= :saleCountEnd");
            paramMap.put("saleCountEnd", saleCountEnd);
        }

        if (baseSaleBegin >= 0) {
            condBuilder.append(" and g.cumulativeStocks >= :baseSaleBegin");
            paramMap.put("baseSaleBegin", baseSaleBegin);
        }

        if (baseSaleEnd >= 0) {
            condBuilder.append(" and g.cumulativeStocks<= :baseSaleEnd");
            paramMap.put("baseSaleEnd", baseSaleEnd);
        }

        if (expireAtBegin != null) {
            condBuilder.append(" and g.expireAt > :expireAtBegin");
            paramMap.put("expireAtBegin", expireAtBegin);
        }

        if (expireAtEnd != null) {
            condBuilder.append(" and g.expireAt <= :expireAtEnd");
            paramMap.put("expireAtEnd", expireAtEnd);
        }

        if (materialType != null) {
            condBuilder.append(" and g.materialType=:materialType");
            paramMap.put("materialType", materialType);
        }
        if (isLottery) {
            condBuilder.append(" and g.isLottery=:isLottery");
            paramMap.put("isLottery", isLottery);
        }
        if (isHideOnsale) {
            condBuilder.append(" and g.isHideOnsale=:isHideOnsale");
            paramMap.put("isHideOnsale", Boolean.FALSE);
        }
        return condBuilder.toString();
    }

    public String getOrderByExpress() {
        String orderType = StringUtils.isBlank(orderByType) ? "DESC" : orderByType;
        return StringUtils.isBlank(orderBy) ? "g.createdAt DESC" : orderBy + " " + orderType;
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


    private static String getSolrOrderBy(int orderById) {
        String orderBy;
        switch (orderById) {
            case 1:
                orderBy = "goods.saleCount_l";
                break;
            case 2:
                orderBy = "goods.salePrice_l";
                break;
            case 3:
                orderBy = "goods.discount_l";
                break;
            case 4:
                orderBy = "goods.materialType_s, goods.createdAt_dt"; //电子券优化显示
                break;
            default:
                orderBy = "goods.recommend_i"; //电子券优化显示
                break;
        }
        return orderBy;
    }

    public static boolean isValidAreaId(String id) {
        if (StringUtils.contains(id, ' ')) {
            return false;
        }
        if ("0".equals(id)) {
            return false;
        }
        return true;
    }

    public String getPriceScopeExpress() {
        if (priceFrom.compareTo(BigDecimal.ZERO) == 0 && priceTo.compareTo(BigDecimal.ZERO) == 0) {
            return "全部";
        }
        if (priceTo.compareTo(BigDecimal.ZERO) == 0) {
            return priceFrom + "元以上";
        }
        return priceFrom + "-" + priceTo + "元";
    }

    public String getUrl() {
        return "/goods/list/" + categoryId + '-' + areaId + '-' + brandId + '-' + priceFrom + '-' + priceTo
                + '-' + orderByNum + '-' + orderByTypeNum;
    }


    public boolean isDefault() {
        return categoryId == 0 && !isValidAreaId(areaId) && brandId == 0 && priceFrom.compareTo(BigDecimal.ZERO) == 0
                && priceTo.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 拼接hql的查询条件.
     *
     * @param condStr hql的查询条件
     */
    public GoodsCondition(boolean isResaler, String condStr) {
        String[] args = condStr.split("-");
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("ResalerGoodsCondition is illegal!");
        }
        if (args.length > 0) {
            brandId = StringUtils.isBlank(args[0]) ? 0 : Long
                    .parseLong(args[0]);
        }
        if (args.length > 1) {
            priceFrom = StringUtils.isBlank(args[1]) ? new
                    BigDecimal(0) : new BigDecimal(args[1]);
        }

        if (args.length > 2) {
            priceTo = StringUtils.isBlank(args[2]) ? new
                    BigDecimal(0) : new BigDecimal(args[2]);
        }

        if (args.length > 3) {

            orderByNum = StringUtils.isBlank(args[3]) ? 0 : Integer.parseInt(args[3]);
            orderBy = StringUtils.isBlank(args[3]) ? getOrderBy(0)
                    : getOrderBy(Integer.parseInt(args[3]));
        }


        if (args.length > 5) {
            type = StringUtils.isBlank(args[5]) ? 1 : Integer.parseInt
                    (args[5]);
        }
        if (type == 0) {
            materialType = null;
        } else if (type == 1) {
            materialType = MaterialType.ELECTRONIC;
        } else {
            materialType = MaterialType.REAL;
        }

        if (args.length > 7) {

            orderByTypeNum = StringUtils.isBlank(args[7]) ? 1 : Integer.parseInt
                    (args[7]);
            orderByType = "1".equals(args[7]) ? "DESC" : "ASC";
        }
    }

    /**
     * 分销商查询条件
     *
     * @return sql 查询条件
     */
    public String getResaleFilter(Resaler resaler) {
        StringBuilder sql = new StringBuilder();
        sql.append(" g.deleted = :deleted");
        paramMap.put("deleted", DeletedStatus.UN_DELETED);
        sql.append(" and g.isLottery = false");
        sql.append(" and g.materialType = :materialType");
        paramMap.put("materialType", MaterialType.ELECTRONIC);

        sql.append(" and g.status = :status");
        paramMap.put("status", GoodsStatus.ONSALE);

        if (brandId != 0) {
            sql.append(" and g.brand = :brand");
            Brand brand = new Brand();
            brand.id = brandId;
            paramMap.put("brand", brand);
        }

        if (priceFrom.compareTo(BigDecimal.ZERO) > 0) {
            sql.append(" and (g.originalPrice + g.resaleAddPrice) >=:priceFrom");
            paramMap.put("priceFrom", priceFrom);
        }

        if (priceTo.compareTo(BigDecimal.ZERO) > 0) {
            sql.append(" and (g.originalPrice+g.resaleAddPrice) <=:priceTo");
            paramMap.put("priceTo", priceTo);
        }

        if (materialType != null) {
            sql.append(" and g.materialType = :materialType");
            paramMap.put("materialType", materialType);
        }

        return sql.toString();
    }


    /**
     * 分销商查询条件  过期不显示
     *
     * @return sql 查询条件
     */
    public String getResaleFilterNoExpired(Resaler resaler) {
        StringBuilder sql = new StringBuilder();
        sql.append(" g.deleted = :deleted");


        paramMap.put("deleted", DeletedStatus.UN_DELETED);

        sql.append(" and g.expireAt >=:expireAt");
        paramMap.put("expireAt", DateUtil.getBeginOfDay());

        sql.append(" and g.isLottery = false");
        sql.append(" and g.materialType = :materialType");
        paramMap.put("materialType", MaterialType.ELECTRONIC);

        sql.append(" and g.status = :status");
        paramMap.put("status", GoodsStatus.ONSALE);

        if (brandId != 0) {
            sql.append(" and g.brand = :brand");
            Brand brand = new Brand();
            brand.id = brandId;
            paramMap.put("brand", brand);
        }

        if (priceFrom.compareTo(BigDecimal.ZERO) > 0) {
            sql.append(" and (g.originalPrice+g.resaleAddPrice) <=:priceFrom");
            paramMap.put("priceFrom", priceFrom);
        }

        if (priceTo.compareTo(BigDecimal.ZERO) > 0) {
            sql.append(" and (g.originalPrice+g.resaleAddPrice) <=:priceTo");
            paramMap.put("priceTo", priceTo);
        }

        if (materialType != null) {
            sql.append(" and g.materialType = :materialType");
            paramMap.put("materialType", materialType);
        }

//
//
//        if (expireAt!=null&& !expireAt.before(new Date())) {
//            sql.append(" and g.expireAt = :expireAt");
//            paramMap.put("expireAt", expireAt);
//        }


        return sql.toString();
    }

    @Override
    public String toString() {
        return super.toString() + "[supplierId:" + supplierId + ",categoryId:" + categoryId +
                ",districtId:" + districtId + ",cityId:" + cityId +
                ",areaId:" + areaId + ",brandId:" + brandId + ",priceFrom:" + priceFrom + ",priceTo:" + priceTo +
                ",type:" + type +
                ",name:" + name + ",no:" + no + ",salePriceBegin:" + salePriceBegin + ",salePriceEnd:" + salePriceEnd +
                ",saleCountBegin:" + saleCountBegin + ",saleCountEnd:" + saleCountEnd +
                ",materialType:" + materialType + ",status:" + status + ",baseSaleBegin:" + baseSaleBegin +
                ",baseSaleEnd:" + baseSaleEnd + ",expireAtBegin:" + expireAtBegin + ",expireAtEnd:" + expireAtEnd + "]";
    }

    /**
     * 商品排期查询条件
     *
     * @return
     */
    public String getScheduleFilter() {
        StringBuilder condBuilder = new StringBuilder(" 1=1 ");
        if (StringUtils.isNotBlank(name)) {
            condBuilder.append(" and g.goods.name like :goodsTitle");
            paramMap.put("goodsTitle", "%" + name.trim() + "%");
        }
        if (expireAtBegin != null) {
            condBuilder.append(" and g.expireAt > :expireAtBegin");
            paramMap.put("expireAtBegin", expireAtBegin);
        }

        if (expireAtEnd != null) {
            condBuilder.append(" and g.expireAt <= :expireAtEnd");
            paramMap.put("expireAtEnd", expireAtEnd);
        }

        return condBuilder.toString();
    }
}
