package models.sales;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import models.resale.ResalerLevel;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(name = "goods_level_prices")
public class GoodsLevelPrice extends Model {
    @Required
    @Enumerated(EnumType.STRING)
    public ResalerLevel level;
    @Required
    @Min(0.01)
    public BigDecimal price;
    @Column(name="goods_id")
    public Long goodsId;

    public GoodsLevelPrice(){

    }

    public GoodsLevelPrice(ResalerLevel level, BigDecimal price){
        this.level = level;
        this.price = price;
    }
}
