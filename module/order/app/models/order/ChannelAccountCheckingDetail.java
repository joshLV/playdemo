package models.order;

import java.math.BigDecimal;
import java.util.Date;

/**
 * User: wangjia
 * Date: 13-7-25
 * Time: 上午11:06
 */
public class ChannelAccountCheckingDetail {
    public Date startDay;

    public Date endDay;

    public String accountedAtStr;

    /**
     * 外部订单号
     */
    public String outerOrderNo;

    /**
     * 业务发生金额.
     */
    public BigDecimal businessAmount;

    /**
     * 佣金.
     */
    public BigDecimal commissionFee;

    /**
     * 实际结算金额.
     */
    public BigDecimal settleAmount;

}
