package models;

import java.math.BigDecimal;

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
    public String order;
}
