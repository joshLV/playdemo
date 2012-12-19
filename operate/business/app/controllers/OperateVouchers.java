package controllers;

import models.accounts.Account;
import models.accounts.Voucher;
import models.accounts.VoucherCondition;
import models.accounts.VoucherType;
import models.accounts.util.AccountUtil;
import models.consumer.User;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author likang
 *         Date: 12-12-12
 */
@With(OperateRbac.class)
@ActiveNavigation("voucher")
public class OperateVouchers extends Controller {
    private static final String DECIMAL_FORMAT = "00000";
    private static final int PAGE_SIZE = 20;

    @ActiveNavigation("voucher_index")
    public static void index(VoucherCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new VoucherCondition();
        }
        JPAExtPaginator<Voucher> voucherPage = Voucher.findByCondition(condition,
                pageNumber, PAGE_SIZE);

        render(voucherPage, condition);
    }

    @ActiveNavigation("voucher_assign")
    public static void showAssign(String err) {
        render(err);
    }


    @ActiveNavigation("voucher_generator")
    public static void generator(String err) {
        render(err);
    }

    @ActiveNavigation("voucher_generator")
    public static void generate(String name, String prefix, BigDecimal faceValue, int count, Long uid, Date expire) {

        Pattern pattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{0,9}$");
        Account account = null;
        if (name == null || name.trim().equals("")) {
            generator("券名称不能为空");
        } else if (prefix == null || !pattern.matcher(prefix).matches()) {
            generator("卡号前缀不符合规范");
        } else if (faceValue == null
                || faceValue.compareTo(BigDecimal.ONE) < 0
                || faceValue.compareTo(new BigDecimal("10000")) > 0
                || faceValue.setScale(0, RoundingMode.FLOOR).compareTo(faceValue) != 0) {
            generator("面值不符合要求");
        } else if (count < 1 || count > 9999) {
            generator("数量不符合要求");
        } else if (expire == null) {
            generator("过期时间不能为空");
        } else {
            Voucher voucher = Voucher.find("bySerialNo",
                    prefix + new DecimalFormat("00000").format(1)).first();
            if (voucher != null) {
                generator("卡号前缀已存在");
            } else {
                if (uid != null && uid > 0) {
                    User user = User.findById(uid);
                    if (user != null) {
                        account = AccountUtil.getConsumerAccount(uid);
                    } else {
                        generator("你指定了一个用户，但是这个用不并不存在");
                    }
                }
            }
        }
        Voucher.generate(count, faceValue, name, prefix, account, OperateRbac.currentUser().getId(), VoucherType.OPERATE, expire);

        index(null);

    }
}
