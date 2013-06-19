package facade.order.vo;

import facade.order.translate.OuterOrderMessage;
import facade.order.translate.OuterOrderMessageTranslate;
import models.order.Order;

/**
 * User: tanglq
 * Date: 13-5-30
 * Time: 下午8:15
 */
public class OuterOrderResult {

    public boolean successed;

    public Order order;

    public OuterOrderResultCode resultCode;


    public OuterOrderMessage getOuterOrderMessage(OuterOrderMessageTranslate translate) {
        switch (resultCode) {
            case INVALID_PARTNER:
                return translate.getInvalidPartnerMessage(this);
        }
        return translate.getSuccessMessage(this);
    }
}
