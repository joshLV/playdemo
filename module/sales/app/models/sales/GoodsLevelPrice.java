package models.sales;

import models.resale.ResalerLevel;
import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Money;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "goods_level_prices")
public class GoodsLevelPrice extends Model {
    
    private static final long serialVersionUID = 7163232064793062L;
    
    @ManyToOne
    public Goods goods;
    @Required
    @Enumerated(EnumType.STRING)
    public ResalerLevel level;
    @Required
    @Max(999999)
    @Min(0)
    @Money
    public BigDecimal price;

//    public Float discount;

    public GoodsLevelPrice() {

    }

    public GoodsLevelPrice(Goods goods, ResalerLevel level, BigDecimal price) {
        this.goods = goods;
        this.level = level;
        this.price = price;

//        setDiscount(goods, price);
    }
/*

    private void setDiscount(Goods goods, BigDecimal price) {
        if (goods != null && goods.faceValue != null && price != null && goods.faceValue.compareTo(new
                BigDecimal(0)) > 0) {
            BigDecimal levelSalePrice = goods.faceValue.add(price);
            this.discount = levelSalePrice.divide(goods.faceValue, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal
                    (10)).floatValue();
        } else {
            this.discount = 0f;
        }
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        setDiscount(goods, price);
    }
*/
}
