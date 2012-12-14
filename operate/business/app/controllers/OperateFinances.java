package controllers;

import models.accounts.AccountSequence;
import models.accounts.AccountType;
import static models.accounts.AccountType.SUPPLIER;
import static models.accounts.AccountType.RESALER;
import static models.accounts.AccountType.CONSUMER;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.resale.Resaler;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;
import models.accounts.Account;
import models.accounts.AccountSequence;

/**
 * 财务核帐.
 * <p/>
 * User: sujie
 * Date: 12/14/12
 * Time: 2:13 PM
 */
@With(OperateRbac.class)
@ActiveNavigation("check_accountsequence")
public class OperateFinances extends Controller {

    public static void index() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(supplierList);
    }

    public static void checkAccountSequence(Long supplierId, String resalerLoginName, String consumerLoginName, AccountType accountType) {
        long uid = 0;
        switch (accountType) {
            case SUPPLIER:
                uid = supplierId;
                break;
            case RESALER:
                Resaler resaler = Resaler.findOneByLoginName(resalerLoginName);
                if (resaler != null) {
                    uid = resaler.id;
                }
                break;
            case CONSUMER:
                User user = User.findByLoginName(consumerLoginName);
                if (user != null) {
                    uid = user.id;
                }
                break;
        }
        Account account = AccountUtil.getAccount(uid, accountType);
        AccountSequence accountSequence = AccountSequence.checkAccountAmount(account);
        boolean isOk = accountSequence == null;
        render("OperateFinances/index.html", isOk, accountSequence, supplierId, resalerLoginName, consumerLoginName, accountType);
    }

}