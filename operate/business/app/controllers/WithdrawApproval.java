package controllers;

import models.accounts.*;
import models.admin.SupplierUser;
import models.consumer.User;
import models.consumer.UserInfo;
import models.resale.Resaler;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.List;

/**
 * 审批提现申请
 *
 * @author likang
 *         Date: 12-5-7
 */

@With(OperateRbac.class)
@ActiveNavigation("withdraw_approval_index")
public class WithdrawApproval extends Controller {
    private static final int PAGE_SIZE = 20;

    public static void index(WithdrawBillCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if (condition == null) {
            condition = new WithdrawBillCondition();
        }


        JPAExtPaginator<WithdrawBill> billPage = WithdrawBill.findByCondition(condition,
                pageNumber, PAGE_SIZE);


        render(billPage, condition);
    }

    public static void detail(Long id, Long uid) {
        WithdrawBill bill = WithdrawBill.findById(id);
        List<WithdrawBill> withdrawBillList = WithdrawBill.find("status=? and applier=?", WithdrawBillStatus.SUCCESS, bill.applier).fetch();
        BigDecimal temp = BigDecimal.ZERO;
        Double sum = 0d;
        String supplierFullName = "";
        for (WithdrawBill b : withdrawBillList) {
            sum += temp.add(b.amount).doubleValue();
        }
        if (bill == null) {
            error("withdraw bill not found");
        }
        if (bill.account.accountType == AccountType.SUPPLIER) {
            Supplier supplier = Supplier.findById(uid);
            supplierFullName = supplier.fullName;
        }
        render(bill, uid, sum, supplierFullName);
    }

    public static void approve(Long id, String action, BigDecimal fee, String comment) {
        WithdrawBill bill = WithdrawBill.findById(id);
        Supplier supplier = null;
        SupplierUser supplierUser = null;
        User user = null;
        Resaler resaler = null;
        String[] arrayName = bill.applier.split("-");
        String title = "";
        String mobile = "";
        if (bill.account.accountType == AccountType.SUPPLIER) {
            supplier = Supplier.findById(bill.account.uid);
            supplierUser = SupplierUser.find("loginName=?", arrayName[1]).first();
            if (supplierUser != null) {
                mobile = supplierUser.mobile;
                title = arrayName[0];
            }
        }
        if (bill.account.accountType == AccountType.CONSUMER) {
            user = User.find("loginName=?", bill.applier).first();
            UserInfo userInfo = UserInfo.find("user=?", user).first();
            if (user != null) {
                mobile = user.mobile;
            }
            if (userInfo != null) {
                title = userInfo.fullName;
            }
        }
        if (bill.account.accountType == AccountType.RESALER) {
            resaler = Resaler.find("loginName=?", bill.applier).first();
            if (resaler != null) {
                title = bill.applier;
                mobile = resaler.mobile;
            }
        }
        String sendContent = title + " 申请提现:" + bill.amount;

        if (bill == null || bill.status != WithdrawBillStatus.APPLIED) {
            error("cannot find the withdraw bill or the bill is processed");
            return;
        }
        if (action.equals("agree")) {
            if (fee == null || fee.compareTo(BigDecimal.ZERO) < 0) {
                error("invalid fee");
                return;
            }
            bill.agree(fee, comment);
            sendContent += " 已结款. ";
        } else if (action.equals("reject")) {
            bill.reject(comment);
            sendContent += " 未通过. ";
        }
        if (StringUtils.isNotBlank(comment)) {
            sendContent += "备注:" + comment;
        }

        if (StringUtils.isNotBlank(mobile) && StringUtils.isNotBlank(title)) {
            SMSUtil.send(sendContent, mobile);
        }
        if (supplier != null && StringUtils.isNotBlank(supplier.accountLeaderMobile) && supplierUser != null && !supplier.accountLeaderMobile.equals(supplierUser.mobile)) {
            SMSUtil.send(sendContent, supplier.accountLeaderMobile);
        }
        index(null);
    }
}
