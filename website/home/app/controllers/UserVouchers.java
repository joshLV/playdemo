package controllers;

import com.uhuila.common.constants.DeletedStatus;
import controllers.modules.website.cas.SecureCAS;
import models.accounts.Account;
import models.accounts.Voucher;
import models.accounts.VoucherCondition;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.libs.Codec;
import play.modules.breadcrumbs.BreadcrumbList;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author likang
 *         Date: 12-12-16
 */
@With({
        SecureCAS.class, WebsiteInjector.class
})
public class UserVouchers extends Controller {
    public static int PAGE_SIZE = 15;

    public static void index() {
        BreadcrumbList breadcrumbs = new BreadcrumbList("我的抵用券", "/voucher");
        User user = SecureCAS.getUser();
        Account account = AccountUtil.getConsumerAccount(user.getId());

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        VoucherCondition condition = new VoucherCondition();
        condition.uid = user.getId();
        condition.deletedStatus = DeletedStatus.UN_DELETED;

        JPAExtPaginator<Voucher> voucherList = Voucher.findByCondition(condition, pageNumber, PAGE_SIZE);

        List<Voucher> validVouchers = Voucher.validVouchers(account);
        BigDecimal validValue = BigDecimal.ZERO;
        for (Voucher voucher: validVouchers) {
            validValue = validValue.add(voucher.value);
        }

        render(breadcrumbs, user, account, voucherList, validVouchers, validValue);
    }

    public static void showAssign() {
        BreadcrumbList breadcrumbs = new BreadcrumbList("抵用券领取", "/voucher/assign");
        User user = SecureCAS.getUser();
        Account account = AccountUtil.getConsumerAccount(user.getId());
        String randomID = Codec.UUID();
        String action = "verify";
        render(randomID, breadcrumbs, user, account, action);
    }

    public static void verify(String voucherCode, String code, String randomID) {
        String errMsg = null;
        User user = SecureCAS.getUser();
        Account account = AccountUtil.getConsumerAccount(user.getId());
        String action = "verify";
        Voucher voucher = null;
        String ridA = null;
        String ridB = null;
        if (Cache.get(randomID) == null
                || code == null
                || !((String) Cache.get(randomID)).toLowerCase()
                .equals(code.toLowerCase())) {
            errMsg = "验证码错误";
        } else {
            voucher = Voucher.find("byChargeCode", voucherCode).first();
            if (voucher == null) {
                errMsg = "抵用券充值密码输入错误";
            } else if (voucher.assignedAt != null || voucher.account != null) {
                errMsg = "该抵用券已被使用";
            } else if (voucher.deleted == DeletedStatus.DELETED) {
                errMsg = "该券无法使用";
            } else if (voucher.expiredAt.before(new Date())) {
                errMsg = "该券已过期";
            } else {
                action = "assign";
                ridA = Codec.UUID();
                ridB = Codec.UUID();
                Cache.set(ridA, ridB, "5mn");
                Cache.set(ridB, voucher.getId(), "5mn");
            }
        }

        Cache.delete(randomID);
        randomID = Codec.UUID();
        BreadcrumbList breadcrumbs = new BreadcrumbList("抵用券领取", "/voucher/assign");
        render("UserVouchers/showAssign.html", randomID, errMsg,
                breadcrumbs, user, account, action, voucher, ridA, ridB,
                voucherCode);
    }

    public static void assign(String ridA, String ridB) {
        User user = SecureCAS.getUser();
        Account account = AccountUtil.getConsumerAccount(user.getId());
        String errMsg = null;
        String suc = null;
        String action = "verify";
        if (Cache.get(ridA) == null || Cache.get(ridB) == null
                || !((String) Cache.get(ridA)).equals(ridB)) {
            errMsg = "验证失败";
        } else {
            Voucher voucher = Voucher.findById((Long) Cache.get(ridB));
            if (voucher == null || voucher.assignedAt != null
                    || voucher.account != null) {
                errMsg = "验证失败";
            } else if (voucher.deleted == DeletedStatus.DELETED) {
                errMsg = "该券无法使用";
            } else if (voucher.expiredAt.before(new Date())) {
                errMsg = "该券已过期";
            } else {
                suc = "领取成功";
                voucher.assignedAt = new Date();
                voucher.account = AccountUtil.getConsumerAccount(user.getId());
                voucher.save();
            }
        }
        Cache.delete(ridA);
        Cache.delete(ridB);
        String randomID = Codec.UUID();
        BreadcrumbList breadcrumbs = new BreadcrumbList("抵用券领取", "/voucher/assign");
        render("UserVouchers/showAssign.html", randomID, suc, errMsg,
                breadcrumbs, user, account, action);
    }
}
