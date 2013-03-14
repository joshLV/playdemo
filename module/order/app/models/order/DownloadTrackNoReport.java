package models.order;

import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;

/**
 * 渠道下载运单报表
 * <p/>
 * User: wangjia
 * Date: 13-3-14
 * Time: 上午11:44
 */
public class DownloadTrackNoReport {

    public OrderItems orderItems;
    public String outerGoodsNo;
    public String goodsName;
    /*
       已发货数
    */
    public Long sentCount;

    /**
     * 总金额
     */
    public BigDecimal totalAmount;

    //String outerGoodsNo, String goodsName
    //, Integer sentCount, BigDecimal totalAmount
    public DownloadTrackNoReport(OrderItems orderItems, Long sentCount, BigDecimal totalAmount) {
        this.orderItems = orderItems;


        System.out.println(totalAmount + "===totalAmount>>");
        System.out.println(orderItems.goods.shortName + "===orderItems.goods.shortName>>");
//        this.outerGoodsNo = outerGoodsNo;
//        this.goodsName = goodsName;
        System.out.println(orderItems.id + "===orderItems.id>>");

        System.out.println(sentCount + "===sentCount>>");
        this.sentCount = sentCount;
        this.totalAmount = totalAmount;
    }

    /**
     * 取得按外部商品ID统计的渠道下载运单
     *
     * @param condition
     * @return
     */
    public static List<DownloadTrackNoReport> query(DownloadTrackNoCondition condition) {
        String sql = "select new models.order.DownloadTrackNoReport(oi,count(distinct oi.shippingInfo.id),sum(oi.salePrice*oi.buyNumber))" +
                " from OrderItems oi";
        String groupBy = " group by oi.outerGoodsNo ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy);


        for (String param : condition.getParams().keySet()) {
            query.setParameter(param, condition.getParams().get(param));
        }

        List<DownloadTrackNoReport> resultList = query.getResultList();
        return resultList;
    }
}
