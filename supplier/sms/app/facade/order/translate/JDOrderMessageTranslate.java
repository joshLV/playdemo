package facade.order.translate;

import facade.order.vo.OuterOrderResult;

/**
 * 用于京东订单生成的返回结果处理类
 */
public class JDOrderMessageTranslate implements OuterOrderMessageTranslate<JDOrderMessage> {
    @Override
    public JDOrderMessage getSuccessMessage(OuterOrderResult result) {
        return new JDOrderMessage(200, "success");
    }

    @Override
    public JDOrderMessage getInvalidPartnerMessage(OuterOrderResult result) {
        return new JDOrderMessage(215, "Configuration Error: Invalid Resaler Partner.");
    }

    @Override
    public JDOrderMessage getUnknownMessage(OuterOrderResult result) {
        return new JDOrderMessage(220, "Unknown Error:" + result.message);
    }

    @Override
    public JDOrderMessage getUnbalanceTotalAmountMessage(OuterOrderResult result) {
        return new JDOrderMessage(203, "the total amount does not match the team price and count");
    }

    @Override
    public JDOrderMessage getInvalidMobileMessage(OuterOrderResult result) {
        return new JDOrderMessage(204, "invalid mobile.");
    }

    @Override
    public JDOrderMessage getNotFoundGoodsMessage(OuterOrderResult result) {
        return new JDOrderMessage(208, "can not find goods.");
    }

    @Override
    public JDOrderMessage getInvalidPriceMessage(OuterOrderResult result) {
        return new JDOrderMessage(209, "invalid product price.");
    }

    @Override
    public JDOrderMessage getInventoryNotEnoughMessage(OuterOrderResult result) {
        return new JDOrderMessage(210, "inventory not enough");
    }

    @Override
    public JDOrderMessage getInvalidBuyCountMessage(OuterOrderResult result) {
        return new JDOrderMessage(202, "the buy number must be a positive one");
    }

    @Override
    public JDOrderMessage getConcurrencyRequestMessage(OuterOrderResult result) {
        return new JDOrderMessage(205, "there is another parallel request");
    }
}
