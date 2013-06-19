package facade.order.translate;

import facade.order.vo.OuterOrderResult;

/**
 * User: tanglq
 * Date: 13-6-19
 * Time: 下午6:22
 */
public interface OuterOrderMessageTranslate {

    OuterOrderMessage getSuccessMessage(OuterOrderResult result);

    OuterOrderMessage getInvalidPartnerMessage(OuterOrderResult result);
}
