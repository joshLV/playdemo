package models.yihaodian.groupbuy;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author likang
 *         Date: 12-9-12
 */
public class YHDGroupBuyOrder{
    // 为了正常的使用Gson 进行解析，属性名要与订单参数完全一致
    public String orderCode;
    public Long productId;
    public Integer productNum;
    public BigDecimal orderAmount;
    public Date createTime;
    public Date paidTime;
    public String userPhone;
    public BigDecimal productPrice;
    public Long groupId;
    public String outerGroupId;
    public int lockVersion;
    public Date createdAt;
}
