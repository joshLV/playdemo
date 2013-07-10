package factory.accounts;

import factory.ModelFactory;
import models.accounts.ClearedAccount;
import models.accounts.SettlementStatus;

/**
 * User: wangjia
 * Date: 13-7-10
 * Time: 上午9:40
 */
public class ClearedAccountFactory extends ModelFactory<ClearedAccount> {
    @Override
    public ClearedAccount define() {
        ClearedAccount account = new ClearedAccount();
        account.settlementStatus = SettlementStatus.UNCLEARED;
        return account;
    }
}
