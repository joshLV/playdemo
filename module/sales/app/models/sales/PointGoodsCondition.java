package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.resale.Resaler;
import org.apache.commons.lang.StringUtils;
import play.Logger;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 积分商品查询条件.
 *
/**
 * User: clara
 * Date: 12-8-6
 * Time: 下午1:09
 */


public class PointGoodsCondition implements Serializable {

    private static final long serialVersionUID = 73232320609113062L;

    public static final String SHANGHAI = "021";

    public long supplierId = 0;
    public long categoryId = 0;
    public String cityId = SHANGHAI;
    public String districtId = "0";
    public String areaId = "0";
    public String searchAreaId = "0";
    public long brandId = 0;
    public BigDecimal priceFrom = BigDecimal.ZERO;
    public BigDecimal priceTo = BigDecimal.ZERO;
    public String orderBy = getOrderBy(0);
    public String orderByType = "DESC";
    public int orderByNum = 9;
    public int orderByTypeNum = 0;
    public int type = 0;
    public String name;
    public String no;


    public Long pointPriceBegin;
    public Long pointPriceEnd;

    public Date createdAtBegin;
    public Date createdAtEnd;


    public Integer saleCountBegin;
    public Integer saleCountEnd;
    public MaterialType materialType;
    public GoodsStatus status;
    public long baseSaleBegin = -1;
    public long baseSaleEnd = -1;
    public int priority;
    public boolean isLottery;
    public Date expireAtBegin;
    public Date expireAtEnd;

    private Map<String, Object> paramMap = new HashMap<>();

    public PointGoodsCondition() {

    }

    /**
     * 拼接hql的查询条件.
     *
     * @param condStr hql的查询条件
     */
    public PointGoodsCondition(String condStr) {
        Logger.info("查询条件：%s", condStr);
        String[] args = condStr.split("-");
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("PointGoodsCondition is illegal!");
        }

        Logger.info("categoryId=" + categoryId + ", args[0]=" + args[0]);
        if (args.length > 0) {
            categoryId = StringUtils.isBlank(args[0]) ? 0 : Long
                    .parseLong(args[0]);
        }
        Logger.info("categoryId=" + categoryId);
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
        condBuilder.append(" g.deleted = :deleted and g.status != :notMatchStatus");
        paramMap.put("deleted", DeletedStatus.UN_DELETED);
        paramMap.put("notMatchStatus", GoodsStatus.UNCREATED);

//        if (StringUtils.isNotBlank(name)) {
//            condBuilder.append(" and g.name like :name");
//            paramMap.put("name", "%" + name.trim() + "%");
//        }

        if (StringUtils.isNotBlank(name)) {
            condBuilder.append(" and g.name like :name");
            paramMap.put("name", "%" + name.trim() + "%");
        }


        if (StringUtils.isNotBlank(no)) {
            condBuilder.append(" and g.no like :no ");
            paramMap.put("no", "%" + no.trim() + "%");
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


//        if (pointPriceBegin != null) {
//            condBuilder.append(" and g.pointPrice >= :pointPriceBegin");
//            paramMap.put("pointPriceBegin", pointPriceBegin);
//        }
//
//        if (pointPriceEnd != null) {
//            condBuilder.append(" and g.pointPrice <= :pointPriceEnd");
//            paramMap.put("pointPriceEnd", pointPriceEnd);
//        }

        if (saleCountBegin != null && saleCountBegin >= 0) {
            condBuilder.append(" and g.saleCount >= :saleCountBegin");
            paramMap.put("saleCountBegin", saleCountBegin);
        }

        if (saleCountEnd != null && saleCountEnd >= 0) {
            condBuilder.append(" and g.saleCount <= :saleCountEnd");
            paramMap.put("saleCountEnd", saleCountEnd);
        }

          //添加时间搜索
        if (createdAtBegin != null )
//            createdAtBegin >= 0
        {
            condBuilder.append(" and g.createdAt >= :createdAtBegin");
            paramMap.put("createdAtBegin", createdAtBegin);
        }

        if (createdAtEnd != null)
        // && saleCountEnd >= 0
        {
            condBuilder.append(" and g.createdAt <= :createdAtEnd");
            paramMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }









        if (baseSaleBegin >= 0) {
            condBuilder.append(" and g.baseSale >= :baseSaleBegin");
            paramMap.put("baseSaleBegin", baseSaleBegin);
        }

        if (baseSaleEnd >= 0) {
            condBuilder.append(" and g.baseSale <= :baseSaleEnd");
            paramMap.put("baseSaleEnd", baseSaleEnd);
        }

//        if (expireAtBegin != null) {
//            condBuilder.append(" and g.expireAt > :expireAtBegin");
//            paramMap.put("expireAtBegin", expireAtBegin);
//        }
//
//        if (expireAtEnd != null) {
//            condBuilder.append(" and g.expireAt <= :expireAtEnd");
//            paramMap.put("expireAtEnd", expireAtEnd);
//        }

        if (materialType != null) {
            condBuilder.append(" and g.materialType=:materialType");
            paramMap.put("materialType", materialType);
        }


        return condBuilder.toString();







    }

    public String getOrderByExpress() {
        String orderType = StringUtils.isBlank(orderByType) ? "DESC" : orderByType;
        return StringUtils.isBlank(orderBy) ? "g.createdAt DESC" : orderBy + " " + orderType;
    }

    public String getOrderByCreatedAtDesc() {
        return  "g.createdAt DESC" ;
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
            case 5:
                orderBy="g.createdAt"; //添加时间
            default:
                orderBy = "g.materialType, g.recommend"; //电子券优化显示
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
    public PointGoodsCondition(boolean isResaler, String condStr) {
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
            sql.append(" and g.id in (select g.id from g.levelPrices l where l.level=:level and g.originalPrice+l.price >=:priceFrom)");
            paramMap.put("level", resaler.level);
            paramMap.put("priceFrom", priceFrom);
        }

        if (priceTo.compareTo(BigDecimal.ZERO) > 0) {
            sql.append(" and g.id in (select g.id from g.levelPrices l where l.level=:level and g.originalPrice+l.price <=:priceTo)");
            paramMap.put("level", resaler.level);
            paramMap.put("priceTo", priceTo);
        }

        if (materialType != null) {
            sql.append(" and g.materialType = :materialType");
            paramMap.put("materialType", materialType);
        }

        return sql.toString();
    }

    @Override
    //没有加上 createdAt
    public String toString() {
        return super.toString() + "[supplierId:" + supplierId + ",categoryId:" + categoryId +
                ",districtId:" + districtId + ",cityId:" + cityId +
                ",areaId:" + areaId + ",brandId:" + brandId + ",priceFrom:" + priceFrom + ",priceTo:" + priceTo +
                ",type:" + type +
                ",name:" + name + ",no:" + no + ",salePriceBegin:" + pointPriceBegin + ",salePriceEnd:" + pointPriceEnd +
                ",saleCountBegin:" + saleCountBegin + ",saleCountEnd:" + saleCountEnd +
                ",materialType:" + materialType + ",status:" + status + ",baseSaleBegin:" + baseSaleBegin +
                ",baseSaleEnd:" + baseSaleEnd + ",expireAtBegin:" + expireAtBegin + ",expireAtEnd:" + expireAtEnd + "]";
    }
}
