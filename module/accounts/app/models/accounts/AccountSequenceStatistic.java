package models.accounts;

import java.math.BigDecimal;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 2/19/13
 * Time: 5:13 PM
 */
public class AccountSequenceStatistic {
    public TradeType tradeType;
    public BigDecimal price;
    public Long count = 0l;
    public BigDecimal amount;

    public AccountSequenceStatistic(TradeType tradeType, BigDecimal price, Long count, BigDecimal amount) {
        this.tradeType = tradeType;
        this.price = price;
        this.count = count;
        this.amount = amount;
    }
}

