package models.yihaodian;

import play.db.jpa.Model;

import javax.persistence.Table;

/**
 * @author likang
 */

@Table(name = "yihaodian_order")
public class YihaodianOrder extends Model {
    public String orderNumber;
    public String goodsNumber;

}
