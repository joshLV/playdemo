package facade.order.constant;

/**
 * 通过enum生成不同渠道的返回错误信息.
 */
public enum OuterOrderResultCode {
    SUCCESS,

    UNBALANCE_TOTAL_AMOUNT,   // 总金额与OrderItem计算出来的总金额不一致.
    INVALID_PARTNER,          // 无效的Resaler.Partner
    INVALID_MOBILE,           // 无效的手机号
    NOT_FOUND_GOODS,          // 找不到对应的商品
    INVALID_PRICE,            // 售价低于进价
    INVENTORY_NOT_ENOUGH,     // 库存不足（只检查导入券)
    CONCURRENCY_REQUEST,      // 并发请求
    INVALID_BUY_COUNT         // 购买数量必须大于0
}
