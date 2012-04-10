package models.sales;

import models.resale.ResalerLevel;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Money;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "goods_level_prices")
public class GoodsLevelPrice extends Model {
    @ManyToOne
    public Goods goods;
    @Required
    @Enumerated(EnumType.STRING)
    public ResalerLevel level;
    @Required
    @Min(0.01)
    @Money
    public BigDecimal price;

    public GoodsLevelPrice() {

    }

    public GoodsLevelPrice(Goods goods,ResalerLevel level, BigDecimal price) {
        this.goods = goods;
        this.level = level;
        this.price = price;
    }
}
