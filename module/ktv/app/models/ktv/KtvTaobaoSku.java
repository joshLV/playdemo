package models.ktv;

import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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

    private Date day;

    @Column(name = "time_range")
    private String timeRange;

    @Column(name = "time_range_code")
    private Integer timeRangeCode;

    public BigDecimal price;

    public Integer quantity;

    @Column(name = "created_at")
    public Date createdAt;

    @Transient
    private String properties;

    public Integer getTimeRangeCode() {
        return timeRangeCode;
    }

    public static String humanTimeRange(int start, int end) {
        end = end >= 24 ? end - 24 : end;
        String endStr = end < 8 ? "营业结束" : end + "点";
        String startStr = start < 8 ? "凌晨" + start + "点" : start + "点";
        return startStr + "至" + endStr;
    }

    public void setTimeRangeCode(Integer timeRangeCode) {
        this.timeRangeCode = timeRangeCode;
        this.timeRange = humanTimeRange(timeRangeCode/100, timeRangeCode%100);

        buildProperties();
    }

    public void setTimeRangeCode(int startTime, int duration) {
        int endTime = startTime + duration;
        this.timeRange = humanTimeRange(startTime, endTime);
        this.timeRangeCode = startTime*100 + endTime;
        buildProperties();
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
        this.date = new SimpleDateFormat("M月d日").format(day);
        buildProperties();
    }


    private void buildProperties() {
        this.properties = buildProperties(roomType, timeRange, date);
    }

    public static String buildProperties(String roomType, String timeRange, String date){
        return StringUtils.defaultString(roomType) +
                ";$欢唱时间:" + StringUtils.defaultString(timeRange) +
                ";$日期:" + StringUtils.defaultString(date);
    }
    public static String buidPropertiesWithCodeAndDate(String roomType, Integer timeRangeCode, Date day) {
        String timeRange = humanTimeRange(timeRangeCode/100, timeRangeCode%100);
        return  StringUtils.defaultString(roomType) +
                ";$欢唱时间:" + timeRange +
                ";$日期:" + new SimpleDateFormat("M月d日").format(day);
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
