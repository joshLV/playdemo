package models.accounts;

/**
 * 账户余额不足异常.
 *
 * @author likang
 * Date: 12-5-11
 */

public class BalanceNotEnoughException extends Exception{
    public BalanceNotEnoughException(String message){
        super(message);
    }
    public BalanceNotEnoughException(String message, Throwable e){
        super(message, e);
    }
}
