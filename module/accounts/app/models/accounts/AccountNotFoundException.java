package models.accounts;

/**
 * 账户未找到异常.
 *
 * @author  likang
 * Date: 12-5-11
 */
public class AccountNotFoundException extends Exception{
    public AccountNotFoundException(String message){
        super(message);
    }
    public AccountNotFoundException(String message, Throwable e){
        super(message, e);
    }
}
