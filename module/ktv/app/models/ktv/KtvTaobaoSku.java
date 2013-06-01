package models.ktv;

import models.sales.Goods;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: yan
 * Date: 13-5-7
 * Time: 下午3:21
 */
public class KtvTaobaoSku {

    private static Pattern taobaoOuterIdPattern = Pattern.compile("([A-Z]+)(\\d{8})(\\d+)");

    private KtvRoomType roomType;
    private Date date;
    private int startTime;
    private int duration;

    private BigDecimal price;
    private Integer quantity;
    private Long taobaoSkuId;

    public int getTimeRangeCode () {
        int endTime = startTime + duration;
        return startTime*100 + (endTime >= 24 ? endTime -24 : endTime);
    }

    public String getTaobaoOuterIid() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return roomType + dateFormat.format(date) + getTimeRangeCode();
    }

    public boolean parseTaobaoOuterId(String outerId) {
        Matcher matcher = taobaoOuterIdPattern.matcher(outerId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        if (matcher.matches()) {
            roomType = KtvRoomType.valueOf(matcher.group(1)) ;
            try {
                date =  dateFormat.parse(matcher.group(2));
            } catch (ParseException e) {
                Logger.error("parse taobao outerIid to sku failed: %s", outerId);
                return false;
            }
            int timRangeCode = Integer.parseInt(matcher.group(3));
            startTime = timRangeCode/100;
            int et = timRangeCode%100;
            if (et < startTime) {
                et = et + 24;
            }
            duration = et - startTime;
        }
        return false;
    }

    public String getTaobaoProperties() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M月d日");
        return roomType.getTaobaoId() +
                ";$欢唱时间:" + humanTimeRange(startTime, startTime + duration) +
                ";$日期:" + dateFormat.format(date);

    }

    public KtvRoomType getRoomType() {
        return roomType;
    }

    public static String humanTimeRange(int start, int end) {
        end = end >= 24 ? end - 24 : end;
        String endStr = end < 8 ? "营业结束" : end + "点";
        String startStr = start < 8 ? "凌晨" + start + "点" : start + "点";
        return startStr + "至" + endStr;
    }

    /*
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
    */

    public void setDate(Date date) {
        this.date = date;
    }

    public void setRoomType(KtvRoomType roomType) {
        this.roomType = roomType;
    }

    public void setStartTimeAndDuration(int startTime, int duration) {
        this.startTime = startTime;
        this.duration = duration;

    }

    public Date getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getTaobaoSkuId() {
        return taobaoSkuId;
    }

    public void setTaobaoSkuId(Long taobaoSkuId) {
        this.taobaoSkuId = taobaoSkuId;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
