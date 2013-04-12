package models;

import models.order.Order;
import models.order.OrderItems;
import models.order.OuterOrderPartner;
import models.sales.ResalerProduct;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;

/**
 * 渠道销售汇总商品
 * <p/>
 * User: wangjia
 * Date: 13-4-11
 * Time: 下午4:39
 */
public class ChannelSalesDailyReport {
    /**
     * 发生日期.
     */
    public String date;

    public OuterOrderPartner partner;       //合作伙伴

    public BigDecimal salesAmount;      //销售额

    public BigDecimal refundAmount;   //退款金额

    public BigDecimal cheatedOrderAmount; //刷单金额

    public BigDecimal netSalesAmount;   //净销售额=销售金额-退款金额-刷单金额

    public String loginName;

    //paidAt
    public ChannelSalesDailyReport(String date, Order order, OrderItems orderItems, BigDecimal netSalesAmount) {
        this.date = date;
        this.salesAmount = netSalesAmount;
        this.netSalesAmount = netSalesAmount;
        this.loginName = order.getResaler().loginName;
        ResalerProduct resalerProduct = ResalerProduct.find("goods =?", orderItems.goods).first();
        this.partner = resalerProduct.partner;
    }

    //refundAt
    public ChannelSalesDailyReport(String date, Order order, BigDecimal refundAmount, OrderItems orderItems) {
        this.date = date;
        this.refundAmount = refundAmount;
        this.loginName = order.getResaler().loginName;
        ResalerProduct resalerProduct = ResalerProduct.find("goods =?", orderItems.goods).first();
        this.partner = resalerProduct.partner;
    }

    //cheatedOrder
    public ChannelSalesDailyReport(String date, BigDecimal cheatedOrderAmount, Order order, OrderItems orderItems) {
        this.date = date;
        this.cheatedOrderAmount = cheatedOrderAmount;
        this.loginName = order.getResaler().loginName;
        ResalerProduct resalerProduct = ResalerProduct.find("goods =?", orderItems.goods).first();
        this.partner = resalerProduct.partner;
    }


    /**
     * 渠道销售汇总日报表统计
     *
     * @param condition
     * @return
     */
    public static List<ChannelSalesDailyReport> query(
            ChannelSalesDailyReportCondition condition) {
        //paidAt  (ecoupon+real)
        String sql = "select new models.ChannelSalesDailyReport(str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),o,r,sum(r.salePrice*r.buyNumber-r.rebateValue)" +
                ") from OrderItems r,Order o,Resaler b where r.order=o and o.userId=b.id ";
        String groupBy = " group by str(year(r.order.paidAt))||'-'||str(month(r.order.paidAt))||'-'||str(day(r.order.paidAt)),r.order.userId";
        Query query = JPA.em()
                .createQuery(sql + condition.getPaidAtFilter() + groupBy + " order by r.order.paidAt desc");
        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }
        List<ChannelSalesDailyReport> paidResultList = query.getResultList();
        return paidResultList;

    }
}
