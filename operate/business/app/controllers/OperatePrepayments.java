package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.accounts.Account;
import models.accounts.AccountSequence;
import models.accounts.AccountType;
import models.accounts.ClearedAccount;
import models.accounts.SettlementStatus;
import models.order.Prepayment;
import models.order.PrepaymentHistory;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static play.Logger.warn;

/**
 * 预付款.
 * <p/>
 * User: sujie
 * Date: 11/22/12
 * Time: 11:52 AM
 */
@With(OperateRbac.class)
@ActiveNavigation("prepayments_index")
public class OperatePrepayments extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 获取券号列表页.
     */
    @ActiveNavigation("prepayments_index")
    public static void index(Long supplierId) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        ModelPaginator<Prepayment> prepaymentPage = Prepayment.getPage(pageNumber, PAGE_SIZE, supplierId);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(prepaymentPage, supplierList, supplierId);
    }

    public static void show(Long id) {
        Prepayment prepayment = Prepayment.findById(id);
        render(prepayment);
    }

    /**
     * 显示添加页面
     */
    @ActiveNavigation("prepayments_add")
    public static void add() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(supplierList);
    }

    @ActiveNavigation("prepayments_add")
    public static void create(@Valid Prepayment prepayment) {
        String loginName = OperateRbac.currentUser().loginName;

        if (Validation.hasErrors()) {
            List<Supplier> supplierList = Supplier.findUnDeleted();
            for (String key : validation.errorsMap().keySet()) {
                warn("create:      validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            render("OperatePrepayments/add.html", supplierList);
        }

        prepayment.deleted = DeletedStatus.UN_DELETED;
        prepayment.settlementStatus = SettlementStatus.UNCLEARED;
        prepayment.createdAt = new Date();
        prepayment.createdBy = loginName;
        prepayment.expireAt = DateUtil.getEndOfDay(prepayment.expireAt);

        prepayment.create();

        prepayment.refresh();
        Prepayment.toHistoryData(prepayment.id, loginName);
        index(null);
    }

    public static void edit(Long id) {
        Prepayment prepayment = Prepayment.findById(id);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(prepayment, supplierList, id);
    }

    public static void update(Long id, @Valid Prepayment prepayment) {
        String loginName = OperateRbac.currentUser().loginName;

        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                warn("update: validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            render("OperatePrepayments/edit.html", prepayment, id);
        }

        Prepayment.update(id, prepayment, loginName);

        Prepayment.toHistoryData(id, loginName);
        index(null);
    }

    /**
     * 进入预付款结算界面
     */
    public static void settle(Long id) {
        Prepayment prepayment = Prepayment.findById(id);
        Supplier supplier = Supplier.findById(prepayment.supplier.id);
        Account account = Account.find("uid = ? and accountType = ?", supplier.id, AccountType.SUPPLIER).first();
        BigDecimal amount = ClearedAccount.getClearedAmount(account, DateUtils.truncate(new Date(), Calendar.DATE));
        render(supplier, amount, prepayment);
    }


    /**
     * 确认结算预付款
     */
    public static void confirmSettle(Long id, String remark) {
        Prepayment prepayment = Prepayment.findById(id);
        prepayment.withdrawAmount = prepayment.amount;
        prepayment.settlementStatus = SettlementStatus.CLEARED;
        prepayment.settleRemark = remark;
        prepayment.save();
        //开始结算预付款并标记相应的ClearedAccount为Cleared
        Prepayment.confirmSettle(prepayment, OperateRbac.currentUser().loginName);
        index(null);
    }

    public static void refreshClearedAccountData() {
        List<Account> accountList = Account.find("(accountType = ? or accountType = ?) and id=82646",
                AccountType.SUPPLIER,
                AccountType.SHOP).fetch();
        ClearedAccount clearedAccount;
        Date fromDate = DateUtil.stringToDate("2013-06-26 00:00:00", "yyyy-MM-dd HH:mm:ss");
        Date toDate = DateUtil.stringToDate("2013-06-26 23:59:59", "yyyy-MM-dd HH:mm:ss");
        System.out.println("accountList.size() = " + accountList.size());
//        for (Account account : accountList) {
//            List<AccountSequence> sequences = AccountSequence.find(
//                    " account=?  and settlementStatus=? and createdAt <?",
//                    account, SettlementStatus.UNCLEARED, toDate).fetch();
//            for (AccountSequence sequence : sequences) {
//                sequence.settlementStatus = SettlementStatus.CLEARED;
//                sequence.save();
//            }
//            clearedAccount = new ClearedAccount();
//            clearedAccount.date = toDate;
//            clearedAccount.accountId = account.id;
//            clearedAccount.amount = AccountSequence.getClearAmount(account, fromDate,
//                    clearedAccount.date);
//            System.out.println(" clearedAccount.amount = " + clearedAccount.amount);
//            clearedAccount.accountSequences = sequences;
//            clearedAccount.save();
//        }

//        for (int i = 0; i < 10; i++) {
//            fromDate = DateUtils.truncate(DateUtils.addDays(new Date(), -1 - i), Calendar.DATE);
//            toDate = DateUtils.truncate(DateUtils.addDays(new Date(), -i), Calendar.DATE);
        fromDate = DateUtil.stringToDate("2013-06-27 00:00:00", "yyyy-MM-dd HH:mm:ss");
        toDate = DateUtil.stringToDate("2013-06-27 23:59:59", "yyyy-MM-dd HH:mm:ss");
        System.out.println("fromDate = " + fromDate);
        System.out.println("toDate = " + toDate);
        for (Account account : accountList) {
            List<AccountSequence> sequences = AccountSequence.find(
                    " account=?  and settlementStatus=? and createdAt >=? and createdAt <?  ",
                    account, SettlementStatus.UNCLEARED, fromDate, toDate).fetch();

            clearedAccount = new ClearedAccount();
            clearedAccount.date = toDate;
            clearedAccount.accountId = account.id;
            clearedAccount.amount = AccountSequence.getClearAmount(account, fromDate,
                    clearedAccount.date);
            for (AccountSequence sequence : sequences) {
                sequence.settlementStatus = SettlementStatus.CLEARED;
                sequence.save();
            }
            System.out.println(" clearedAccount.amount = " + clearedAccount.amount);
            clearedAccount.accountSequences = sequences;
            clearedAccount.save();
        }
//        }
    }


    public static void history(Long id) {
        List<PrepaymentHistory> historyList = PrepaymentHistory.find("prepaymentId=? order by id desc", id).fetch();
        render("OperatePrepayments/history.html", historyList);
    }

    public static void delete(Long id) {
        Prepayment prepayment = Prepayment.findById(id);
        if (prepayment != null) {
            prepayment.deleted = DeletedStatus.DELETED;
            prepayment.save();
        }
        index(null);
    }

    /**
     * 冲正界面
     */
    public static void balanceBill() {
        List<Supplier> supplierList = Supplier.findUnDeletedWithPrepayment();
        render(supplierList);
    }

    /**
     * 进行冲正
     */
    public static void confirmBalanceBill() {
        index(null);
    }
}
