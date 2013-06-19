package facade.order.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * User: tanglq
 * Date: 13-6-19
 * Time: 下午4:15
 */
public class OuterOrderItemVO {

    /**
     * 渠道商品项目ID.
     */
    public Long venderTeamId;

    /**
     * 渠道单价.
     */
    public BigDecimal price;

    /**
     * 商品数量.
     */
    public long count;

    public List<OuterECouponVO> eCoupons;


    public static OuterOrderItemVO build() {
        return new OuterOrderItemVO();
    }

    public OuterOrderItemVO venderTeamId(Long value) {
        this.venderTeamId = value;
        return this;
    }

    public OuterOrderItemVO price(BigDecimal value) {
        this.price = value;
        return this;
    }

    public OuterOrderItemVO count(long value) {
        this.count = value;
        return this;
    }


    public OuterOrderItemVO addECouponVO(OuterECouponVO eCouponVO) {
        if (this.eCoupons == null) {
            this.eCoupons = new ArrayList<>();
        }
        this.eCoupons.add(eCouponVO);
        return this;
    }

}
