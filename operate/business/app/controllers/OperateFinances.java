package controllers;

import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.util.AccountSequenceUtil;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import models.resale.Resaler;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

import static play.Logger.info;

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
        List<Supplier> supplierList = Supplier.findUnDeleted();

        long uid = 0;
        if (accountType == null) {
            render("OperateFinances/index.html", supplierList);
        }
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
        render("OperateFinances/index.html", supplierList, isOk, accountSequence, supplierId, resalerLoginName, consumerLoginName, accountType);
    }

    public static void fixAccountSequence(Long accountId) {
        List<Account> accounts = new ArrayList<>();
        Account account = Account.findById(accountId);
        if (account != null) {
            accounts.add(account);
        }
        //检查并修复财务流水
        info("=====Begin to check and fix sequence balance");
        AccountSequenceUtil.checkAndFixBalance(accounts, null);
        info("=====End to check and fix sequence balance");

        //修复后再次检查并修复帐号的余额
        info("=====Begin to check and fix account amount");
        AccountSequenceUtil.checkAndFixAccountAmount(accounts);
        info("=====End to check and fix account amount");

        List<Supplier> supplierList = Supplier.findUnDeleted();
        render("OperateFinances/index.html", supplierList, account);
    }
}
