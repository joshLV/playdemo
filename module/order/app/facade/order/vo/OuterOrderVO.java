package facade.order.vo;

import models.resale.Resaler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于生成外部订单的VO对象，包装生成外部订单的所有参数.
 */
public class OuterOrderVO {
    /**
     * 分销商.
     */
    public Resaler resaler;

    /**
     * 订单总价.
     */
    public BigDecimal totalAmount;

    public String mobile;
    public String outerOrderId;

    public String message;

    public List<OuterOrderItemVO> orderItems;

    public static OuterOrderVO build(Resaler resaler) {
        OuterOrderVO outerOrderVO = new OuterOrderVO();
        outerOrderVO.resaler = resaler;
        return outerOrderVO;
    }

    public OuterOrderVO totalAmount(BigDecimal value) {
        this.totalAmount = value;
        return this;
    }


    public OuterOrderVO mobile(String value) {
        this.mobile = value;
        return this;
    }

    public OuterOrderVO outerOrderId(String value) {
        this.outerOrderId = value;
        return this;
    }

    public OuterOrderVO message(String value) {
        this.message = value;
        return this;
    }

    public OuterOrderVO addItem(OuterOrderItemVO orderItemVO) {
        if (this.orderItems == null) {
            this.orderItems = new ArrayList<>();
        }
        this.orderItems.add(orderItemVO);
        return this;
    }

}
