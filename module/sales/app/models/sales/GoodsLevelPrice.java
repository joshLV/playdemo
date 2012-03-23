package models.sales;

import models.resale.ResalerLevel;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;

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
    public BigDecimal price;

    public GoodsLevelPrice() {

    }

    public GoodsLevelPrice(ResalerLevel level, BigDecimal price) {
        this.level = level;
        this.price = price;
    }
}
