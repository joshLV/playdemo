package models.accounts;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户资金明细汇总信息.
 * <p/>
 * User: sujie
 * Date: 5/4/12
 * Time: 10:11 AM
 */
public class AccountSequenceSummary {
    long vostroCount;       //来账笔数,收入笔数
    BigDecimal vostroAmount; //来账总额,收入总额

    long nostroCount;       //往账笔数,支出笔数
    BigDecimal nostroAmount; //往账总额,支出总额

    public AccountSequenceSummary() {

    }

    /**
     * @param params Object[],数组的每个对象的含义分别为  1:flag,2:count,3:amount
     */
    public AccountSequenceSummary(List<Object[]> params) {
        for (Object[] param : params) {
            if (param[0] instanceof AccountSequenceFlag) {
                AccountSequenceFlag flag = (AccountSequenceFlag) param[0];
                switch (flag){
                    case VOSTRO:
                        vostroCount = (Long) param[1];
                        vostroAmount = (BigDecimal) param[2];
                        break;
                    case NOSTRO:
                        nostroCount = (Long) param[1];
                        nostroAmount = (BigDecimal) param[2];
                        break;
                }
            }

        }
    }
}
