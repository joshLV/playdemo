package models.ktv;

import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * User: yan
 * Date: 13-5-7
 * Time: 下午3:21
 */
@Table(name = "ktv_taobao_skus")
@Entity
public class KtvTaobaoSku extends Model {

    @ManyToOne
    @JoinColumn(name = "goods_id")
    public Goods goods;

    @Column(name = "room_type")
    private String roomType;

    private String date;

    @Column(name = "time_range")
    private String timeRange;

    public BigDecimal price;

    public Integer quantity;


    @Column(name = "created_at")
    public Date createdAt;


    @Transient
    private String properties;

    private void buildProperties() {
        this.properties = StringUtils.defaultString(this.roomType) +
                ";$欢唱时间:" + StringUtils.defaultString(this.timeRange) +
                ";$日期:" + StringUtils.defaultString(this.date);
//        this.properties = StringUtils.defaultString(this.roomType) + ";$日期:"
//                + StringUtils.defaultString(this.date) + ";$欢唱时间:"
//                + StringUtils.defaultString(this.timeRange);
    }

    public String getProperties() {
        if (properties == null) {
            buildProperties();
        }
        return properties;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
        buildProperties();
    }

    public void setDate(String date) {
        this.date = date;
        buildProperties();
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        buildProperties();
    }

    public String getRoomType() {
        return roomType;
    }

    public String getDate() {
        return date;
    }

    public String getTimeRange() {
        return timeRange;
    }
}
