package facade.order.translate;

import facade.order.vo.OuterOrderResult;

/**
 * User: tanglq
 * Date: 13-6-19
 * Time: 下午6:22
 */
public interface OuterOrderMessageTranslate<T extends OuterOrderMessage> {

    T getSuccessMessage(OuterOrderResult result);

    T getInvalidPartnerMessage(OuterOrderResult result);

    T getUnknownMessage(OuterOrderResult result);

    T getUnbalanceTotalAmountMessage(OuterOrderResult result);

    T getInvalidMobileMessage(OuterOrderResult result);

    T getNotFoundGoodsMessage(OuterOrderResult result);

    T getInvalidPriceMessage(OuterOrderResult result);

    T getInventoryNotEnoughMessage(OuterOrderResult result);

    T getInvalidBuyCountMessage(OuterOrderResult result);

    T getConcurrencyRequestMessage(OuterOrderResult result);
}
