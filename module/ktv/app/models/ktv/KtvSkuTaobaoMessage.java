package models.ktv;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author likang
 *         Date: 13-5-31
 */
public class KtvSkuTaobaoMessage implements Serializable{
    private static final long serialVersionUID = -8231121251842194450L;
    public int action;

    public Long skuId;
    public Long resalerProductId;
    public Long goodsId;
    public String roomType;
    public String date;
    public Date day;
    public String timeRange;
    public Integer timeRangeCode;
    public BigDecimal price;
    public Integer quantity;
    public Date createdAt;
}
