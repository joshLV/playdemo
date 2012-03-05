package models.accounts;

/**
 * 充值状态
 *
 * User: likang
 */
public enum ChargeStatus {
    UNCHARGED,  //待充值
    CHARGING,   //正在充值
    SUCCESS,    //成功
    FAILED      //失败
}
