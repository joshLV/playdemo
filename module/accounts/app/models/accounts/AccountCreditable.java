package models.accounts;

/**
 * @author likang
 * Date: 12-6-7
 */
public enum AccountCreditable {
    YES,    // 账户允许金额小于0
    NO      // 账户不允许出现金额小于0
}
