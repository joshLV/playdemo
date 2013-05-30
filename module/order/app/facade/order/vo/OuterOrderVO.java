package facade.order.vo;

import java.math.BigDecimal;

/**
 * 用于生成外部订单的VO对象，包装生成外部订单的所有参数.
 */
public class OuterOrderVO {
    public Long venderTeamId;
    public BigDecimal teamPrice;
    public long count;
    public String mobile;

    public static OuterOrderVO build() {
        return new OuterOrderVO();
    }

}
