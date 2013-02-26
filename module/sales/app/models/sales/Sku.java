package models.sales;

import models.supplier.Supplier;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * <p/>
 * User: yanjy
 * Date: 13-2-26
 * Time: 下午4:34
 */
@Entity
@Table(name = "sku")
public class Sku extends Model {
    @ManyToOne
    public Goods goods;

    //SKU名称
    @Required
    @MaxSize(value = 500)
    public String name;
    //市场价
    @Required
    @Column(name = "market_price")
    public BigDecimal marketPrice;
    // 库存（初始0）
    public Long stock = 0L;

    // SKU编码 【2位类别编码+4位商户编码+2位品牌编码+2位流水码】
    @Column(name = "code")
    public String code;
    // 发货商户
    @Required
    public Long supplierId;

    // 品牌
    @Required
    public Long brandId;
    // 类别
    @Required
    public Long categoryId;

}