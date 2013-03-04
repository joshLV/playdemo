package models.sales;

/**
 * 库存变动行为类型.
 * <p/>
 * User: sujie
 * Date: 3/4/13
 * Time: 4:25 PM
 */
public enum StockActionType {
    IN("J"),  //入库
    OUT("C"), //出库

    REFUND("T");//退货

    String _code;

    StockActionType(String code) {
        _code = code;
    }

    public String getCode() {
        return _code;
    }
}
