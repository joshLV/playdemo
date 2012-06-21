package models.sales;

/**
 * 商品的物质类型.
 * <p/>
 * User: sujie
 * Date: 2/20/12
 * Time: 12:37 PM
 */
public enum MaterialType {
    REAL(0),
    ELECTRONIC(1);
    
    int _value;
    MaterialType(int value) {
        _value = value;
    }
    
    public int getIntValue() {
        return _value;
    }
}
