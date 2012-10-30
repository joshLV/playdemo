package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import org.apache.commons.lang.StringUtils;
import play.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * WWW搜索商品时用到的商品条件类.
 * <p/>
 * User: sujie
 * Date: 10/30/12
 * Time: 5:08 PM
 */
public class GoodsWebsiteCondition implements Serializable {

    private static final long serialVersionUID = 73232320609113062L;

    public static final String SHANGHAI = "021";

    public long supplierId = 0;
    public long parentCategoryId = 0;
    public long categoryId = 0;
    public String cityId = SHANGHAI;
    public String districtId = "0";
    public String areaId = "0";
    public String searchAreaId = "0";
    public String orderBy = getOrderBy(0);
    public String solrOrderBy = getSolrOrderBy(0);
    public String orderByType = "desc";
    public int orderByNum = 9;
    public int orderByTypeNum = 0;
    public int type = 0;
    public String name;
    public String no;


    public MaterialType materialType;
    public GoodsStatus status;
    public int priority;
    public Date expireAt;

    private Map<String, Object> paramMap = new HashMap<>();
    public String keywords;

    public GoodsWebsiteCondition() {

    }
    /**
     * 拼接hql的查询条件.
     *
     * @param condStr hql的查询条件
     */
    public GoodsWebsiteCondition(String condStr) {
        Logger.info("查询条件：%s", condStr);
        String[] args = condStr.split("-");
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("GoodsWebsiteCondition is illegal!");
        }

        if (args.length > 0) {
            categoryId = StringUtils.isBlank(args[0]) ? 0 : Long
                    .parseLong(args[0]);
        }
        if (args.length > 1) {
            searchAreaId = StringUtils.isBlank(args[1]) ? SHANGHAI : args[1];
            setAreaBySearchId();
        }

        if (args.length > 2) {
            orderByNum = StringUtils.isBlank(args[2]) ? 0 : Integer.parseInt(args[2]);
            orderBy = StringUtils.isBlank(args[2]) ? getOrderBy(0)
                    : getOrderBy(Integer.parseInt(args[2]));
            solrOrderBy = StringUtils.isBlank(args[2]) ? getSolrOrderBy(0)
                    : getSolrOrderBy(Integer.parseInt(args[2]));
        }
        if (args.length > 3) {
            orderByTypeNum = StringUtils.isBlank(args[3]) ? 1 : Integer.parseInt
                    (args[3]);
            orderByType = "1".equals(args[3]) ? "desc" : "asc";
        }
        if (args.length > 4) {
            materialType = StringUtils.isBlank(args[4]) ? MaterialType.ELECTRONIC :
                    MaterialType.values()[Integer.parseInt(args[4])];
        }
    }

    private void setAreaBySearchId() {
        System.out.println("searchAreaId:" + searchAreaId);
        if (searchAreaId.length() == 8) {
            districtId = searchAreaId.substring(0, 5);
            areaId = searchAreaId;
        } else if (searchAreaId.length() == 5) {
            districtId = searchAreaId;
            areaId = "0";
        }
    }

    public GoodsWebsiteCondition(String condition, String keywords) {
        this(condition);
        this.keywords = keywords;
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

        if (supplierId != 0) {
            condBuilder.append(" and g.supplierId = :supplierId");
            paramMap.put("supplierId", supplierId);
        }
        if (categoryId != 0) {
            condBuilder.append(" and g.id in (select g.id from " +
                    "g.categories c where c.id = :categoryId or (c.parentCategory is not null and c.parentCategory.id=:categoryId))");
            paramMap.put("categoryId", categoryId);
        }

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
        }

        if (materialType != null) {
            condBuilder.append(" and g.materialType=:materialType");
            paramMap.put("materialType", materialType);
        }
        return condBuilder.toString();
    }

    public String getOrderByExpress() {
        String orderType = StringUtils.isBlank(orderByType) ? "desc" : orderByType;
        return StringUtils.isBlank(orderBy) ? "g.createdAt desc" : orderBy + " " + orderType;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public String getOrderByTypeExpress() {
        if (orderBy.equals("g.firstOnSaleAt") || solrOrderBy.equals("goods.firstOnSaleAt_dt")) {
            if (orderByType.equals("asc")) {
                return "从早到晚";
            } else {
                return "从晚到早";
            }
        } else {
            if (orderByType.equals("asc")) {
                return "从低到高";
            } else {
                return "从高到低";
            }
        }
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
                orderBy = "g.firstOnSaleAt";
                break;
            case 4:
                orderBy = "g.materialType, g.firstOnSaleAt"; //电子券优化显示
                break;
            default:
                orderBy = "g.materialType, g.recommend"; //电子券优化显示
                break;
        }
        return orderBy;
    }


    public static String getSolrOrderBy(int orderById) {
        String orderBy;
        switch (orderById) {
            case 1:
                orderBy = "goods.saleCount_l";
                break;
            case 2:
                orderBy = "goods.salePrice_l";
                break;
            case 3:
                orderBy = "goods.firstOnSaleAt_dt";
                break;
            case 4:
                orderBy = "goods.materialType_s, goods.firstOnSaleAt_dt"; //电子券优化显示
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
        return !"0".equals(id);
    }

    public String getUrl() {
        String url = "/q/" + categoryId + '-' + searchAreaId + '-' + orderByNum + '-' + orderByTypeNum;
        if (materialType != null) {
            url += '-' + materialType.getIntValue();
        }
        if (keywords != null) {
            keywords.replace(" ", "+");
            url += "?s=" + keywords;
        }
        return url;
    }

    public boolean isDefault() {
        return categoryId == 0 && !isValidAreaId(areaId);
    }

    public GoodsWebsiteCondition buildUrl(String queryProperty, Object value) {
        try {
            GoodsWebsiteCondition condition = (GoodsWebsiteCondition) this.clone();
            if (StringUtils.isBlank(queryProperty)) {
                return this;
            }
            if (queryProperty.equals("categoryId")) {
                condition.categoryId = (Long) value;
                condition.searchAreaId = "0";
                setAreaBySearchId();
            }
            if (queryProperty.equals("searchAreaId")) {
                condition.searchAreaId = (String) value;
                setAreaBySearchId();
            }
            if (queryProperty.equals("orderByNum")) {
                condition.orderByNum = (Integer) value;
            }
            if (queryProperty.equals("orderByTypeNum")) {
                condition.orderByTypeNum = (Integer) value;
            }
            if (queryProperty.equals("materialType")) {
                condition.materialType = (MaterialType) value;
            }
            return condition;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return this;
    }

    @Override
    public String toString() {
        return super.toString() + "[supplierId:" + supplierId + ",categoryId:" + categoryId +
                ",districtId:" + districtId + ",cityId:" + cityId +
                ",areaId:" + areaId + ",type:" + type +
                ",name:" + name + ",no:" + no + ",materialType:" + materialType + ",status:" + status + "]";
    }

    /**
     * Creates and returns a copy of this object.  The precise meaning
     * of "copy" may depend on the class of the object. The general
     * intent is that, for any object {@code x}, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be {@code true}, but these are not absolute requirements.
     * While it is typically the case that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be {@code true}, this is not an absolute requirement.
     *
     * By convention, the returned object should be obtained by calling
     * {@code super.clone}.  If a class and all of its superclasses (except
     * {@code Object}) obey this convention, it will be the case that
     * {@code x.clone().getClass() == x.getClass()}.
     *
     * By convention, the object returned by this method should be independent
     * of this object (which is being cloned).  To achieve this independence,
     * it may be necessary to modify one or more fields of the object returned
     * by {@code super.clone} before returning it.  Typically, this means
     * copying any mutable objects that comprise the internal "deep structure"
     * of the object being cloned and replacing the references to these
     * objects with references to the copies.  If a class contains only
     * primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by {@code super.clone}
     * need to be modified.
     *
     * The method {@code clone} for class {@code Object} performs a
     * specific cloning operation. First, if the class of this object does
     * not implement the interface {@code Cloneable}, then a
     * {@code CloneNotSupportedException} is thrown. Note that all arrays
     * are considered to implement the interface {@code Cloneable} and that
     * the return type of the {@code clone} method of an array type {@code T[]}
     * is {@code T[]} where T is any reference or primitive type.
     * Otherwise, this method creates a new instance of the class of this
     * object and initializes all its fields with exactly the contents of
     * the corresponding fields of this object, as if by assignment; the
     * contents of the fields are not themselves cloned. Thus, this method
     * performs a "shallow copy" of this object, not a "deep copy" operation.
     *
     * The class {@code Object} does not itself implement the interface
     * {@code Cloneable}, so calling the {@code clone} method on an object
     * whose class is {@code Object} will result in throwing an
     * exception at run time.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException if the object's class does not
     *                                    support the {@code Cloneable} interface. Subclasses
     *                                    that override the {@code clone} method can also
     *                                    throw this exception to indicate that an instance cannot
     *                                    be cloned.
     * @see Cloneable
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        GoodsWebsiteCondition cond = new GoodsWebsiteCondition();
        cond.keywords = this.keywords;
        cond.supplierId = this.supplierId;
        cond.parentCategoryId = this.parentCategoryId;
        cond.categoryId = this.categoryId;
        cond.cityId = this.cityId;
        cond.districtId = this.districtId;
        cond.areaId = this.areaId;
        cond.searchAreaId = this.searchAreaId;
        cond.orderBy = this.orderBy;
        cond.solrOrderBy = this.solrOrderBy;
        cond.orderByType = this.orderByType;
        cond.orderByNum = this.orderByNum;
        cond.orderByTypeNum = this.orderByTypeNum;
        cond.name = this.name;
        cond.no = this.no;
        cond.materialType = this.materialType;
        cond.status = this.status;
        cond.priority = this.priority;
        cond.expireAt = this.expireAt;
        cond.paramMap = new HashMap<>();
        cond.paramMap.putAll(this.paramMap);
        return cond;
    }
}
