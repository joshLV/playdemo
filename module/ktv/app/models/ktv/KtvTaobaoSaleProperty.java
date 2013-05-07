package models.ktv;

import models.sales.Goods;
import models.sales.ResalerProduct;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * User: yan
 * Date: 13-5-7
 * Time: 下午3:21
 */
@Table(name = "ktv_taobao_sale_properties")
@Entity
public class KtvTaobaoSaleProperty extends Model {

    @ManyToOne
    @JoinColumn(name = "goods_id")
    public Goods goods;

    @Column(name = "room_type")
    public String roomType;

    public String date;

    @Column(name = "time_range")
    public String timeRange;

    public BigDecimal price;

    public Integer quantity;


    @Column(name = "created_at")
    public Date createdAt;


    @Transient
    public String identity;

    public void makeIdentity() {
        identity = roomType + date + timeRange;
    }
}
