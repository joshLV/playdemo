package models.ktv;

import play.Logger;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private static String[] weekNames = {"日", "一", "二", "三", "四", "五", "六"};

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

    public KtvTaobaoSku parseTaobaoOuterId(String outerId) {
        if (outerId == null) {
            return null;
        }
        Matcher matcher = taobaoOuterIdPattern.matcher(outerId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        if (matcher.matches()) {
            roomType = KtvRoomType.valueOf(matcher.group(1)) ;
            try {
                date =  dateFormat.parse(matcher.group(2));
            } catch (ParseException e) {
                Logger.error("parse taobao outerIid to sku failed: %s", outerId);
                return null;
            }
            int timRangeCode = Integer.parseInt(matcher.group(3));
            startTime = timRangeCode/100;
            int et = timRangeCode%100;
            if (et < startTime) {
                et = et + 24;
            }
            duration = et - startTime;

            return this;
        }
        return null;
    }

    public String getTaobaoProperties() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M月d日");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String weekName = weekNames[calendar.get(Calendar.DAY_OF_WEEK) - 1];

        return roomType.getTaobaoId() +
                ";$欢唱时间:" + humanTimeRange(startTime, startTime + duration) +
                ";$日期:" + dateFormat.format(date) + "(周" + weekName + ")";
    }

    public KtvRoomType getRoomType() {
        return roomType;
    }

    public static String humanTimeRange(int start, int end) {
        end = end >= 24 ? end - 24 : end;
        String endStr = end < 6 ? "凌晨" + end + "点" : end + "点";
        String startStr = start < 6 ? "凌晨" + start + "点" : start + "点";
        return startStr + "至" + endStr;
    }

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
