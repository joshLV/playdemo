package models.order;

/**
 * 冻结单张券号时的选项.
 * <p/>
 * User: wangjia
 * Date: 13-1-21
 * Time: 下午4:20
 */
public enum ECouponFreezedReason {
    ISCHEATEDORDER,   //视惠刷单
    ISSUPPLIERCHEATEDORDER, //商户刷单并券消费
    UNABLEVERIFY,       //无法验证
    OTHERS         //其他原因
}
