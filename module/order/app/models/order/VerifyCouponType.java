package models.order;

/**
 * <p/>
 * User: yanjy
 * Date: 12-6-18
 * Time: 上午10:48
 */
public enum VerifyCouponType {
    CLERK_MESSAGE,      //店员短信验证
    CONSUMER_MESSAGE,   //消费者短信验证
    SHOP,               //门店验证
    TELEPHONE,          //电话验证
    WEIXIN,             //微信验证
    OP_VERIFY,          //运营代理验证
    IMPORT_VERIFY,      //导入券自动验证
    AUTO_VERIFY       //券自动验证
}
