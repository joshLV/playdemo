package facade.order.vo;

import facade.order.translate.OuterOrderMessage;
import facade.order.translate.OuterOrderMessageTranslate;
import models.order.ECoupon;
import models.order.OuterOrder;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * User: tanglq
 * Date: 13-5-30
 * Time: 下午8:15
 */
public class OuterOrderResult {

    public OuterOrder outerOrder;

    public OuterOrderResultCode resultCode;

    public String message;

    /**
     * 生成的所有电子券
     */
    public List<ECoupon> eCoupons;

    public <T extends OuterOrderMessage> T getOuterOrderMessage(OuterOrderMessageTranslate<T> translate) {
        Logger.info("OuterOrderResult code: %s, message: %s", resultCode.toString(), message);
        switch (resultCode) {
            case SUCCESS:
                return translate.getSuccessMessage(this);
            case INVALID_PARTNER:
                return translate.getInvalidPartnerMessage(this);
            case UNBALANCE_TOTAL_AMOUNT:
                return translate.getUnbalanceTotalAmountMessage(this);
            case INVALID_MOBILE:
                return translate.getInvalidMobileMessage(this);
            case NOT_FOUND_GOODS:
                return translate.getNotFoundGoodsMessage(this);
            case INVALID_PRICE:
                return translate.getInvalidPriceMessage(this);
            case INVENTORY_NOT_ENOUGH:
                return translate.getInventoryNotEnoughMessage(this);
            case INVALID_BUY_COUNT:
                return translate.getInvalidBuyCountMessage(this);
        }
        return translate.getUnknownMessage(this);
    }

    public static OuterOrderResult build(OuterOrderResultCode resultCode, String message) {
        OuterOrderResult result = new OuterOrderResult();
        result.message = message;
        result.resultCode = resultCode;
        return result;
    }

    public OuterOrderResult outerOrder(OuterOrder value) {
        this.outerOrder = value;
        return this;
    }

    public OuterOrderResult addECoupon(ECoupon eCoupon) {
        if (this.eCoupons == null) {
            this.eCoupons = new ArrayList<>();
        }
        this.eCoupons.add(eCoupon);
        return this;
    }
}
