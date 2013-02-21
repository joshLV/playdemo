package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.Account;
import models.accounts.Voucher;
import models.accounts.VoucherCondition;
import models.accounts.VoucherType;
import models.accounts.util.AccountUtil;
import models.operator.OperateUser;
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
        for (Voucher voucher : voucherPage.getCurrentPage()) {
            setOperatorName(voucher);
        }
        render(voucherPage, condition);
    }

    public static void update(Long id, String action) {
        Voucher voucher = Voucher.findById(id);
        if (voucher != null) {
            if (!StringUtils.isBlank(action)) {
                if (action.equalsIgnoreCase("delete")) {
                    voucher.deleted = DeletedStatus.DELETED;
                    voucher.save();
                    renderJSON("{\"status\":\"ok\"}");
                }
            }
        }
        renderJSON("{\"status\":\"failed\"}");
    }

    @ActiveNavigation("voucher_assign")
    public static void showAssign(String err) {
        render(err);
    }

    @ActiveNavigation("voucher_assign")
    public static void assign(String users, String vouchers, String type) {
        if (StringUtils.isBlank(users) || StringUtils.isBlank(vouchers) || StringUtils.isBlank(type)) {
            showAssign("请输入一点信息啊");
        }
        String[] userIds = users.split("\\r?\\n");
        String[] voucherIds = vouchers.split("\\r?\\n");

        boolean is1to1 = type.equalsIgnoreCase("1to1");
        if (is1to1 && userIds.length != voucherIds.length) {
            showAssign("两边数量要相等");
        }
        Long userId = 0L;
        User user = null;

        StringBuilder err = new StringBuilder("已完成，请注意以下错误（没有就算了）<br/>");
        for (int i = 0; i < userIds.length; i++) {
            if (is1to1 || i == 0) {
                try {
                    userId = Long.parseLong(userIds[i]);
                } catch (Exception e) {
                    err.append("第" + (i + 1) + "个用户,ID：" + userIds[i] + "解析错误<br/>");
                    if (is1to1) continue;
                    else break;
                }
                user = User.findById(userId);
                if (user == null) {
                    err.append("第" + (i + 1) + "个用户,ID：" + userIds[i] + "没找到<br/>");
                    if (is1to1) continue;
                    else break;
                }
            }
            Voucher voucher = Voucher.find("byChargeCode", voucherIds[i]).first();
            if (voucher == null) {
                err.append("第" + (i + 1) + "个券号：" + voucherIds[i] + "没找到<br/>");
                continue;
            }
            if (voucher.expiredAt.before(new Date())) {
                err.append("第" + (i + 1) + "个券号：" + voucherIds[i] + "是过期的<br/>");
                continue;
            }
            if (voucher.account != null || voucher.assignedAt != null) {
                err.append("第" + (i + 1) + "个券号：" + voucherIds[i] + "已经绑定过了<br/>");
                continue;
            }

            voucher.account = AccountUtil.getConsumerAccount(user.getId());
            voucher.assignedAt = new Date();
            voucher.operatorId = OperateRbac.currentUser().getId();
            voucher.save();
        }

        showAssign(err.toString());
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
                || faceValue.setScale(2, RoundingMode.FLOOR).compareTo(faceValue) != 0) {
            generator("面值不符合要求");
        } else if (count < 1 || count > 9999) {
            generator("数量不符合要求");
        } else if (expire == null || expire.before(new Date())) {
            generator("过期时间不能为空,也不能小于当前时间");
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
        expire = new Date(expire.getTime() - 1000);
        Voucher.generate(count, faceValue, name, prefix, account, OperateRbac.currentUser().getId(), VoucherType.OPERATE, expire);

        index(null);

    }

    private static void setOperatorName(Voucher voucher) {
        if (voucher.operatorId == null) {
            voucher.operatorName = "";
            return;
        }
        if (voucher.voucherType == VoucherType.EXCHANGE) {
            User user = User.findById(voucher.operatorId);
            if (user != null) {
                voucher.operatorName = "消费者兑换：" + user.getShowName();
            }
        } else {
            OperateUser operateUser = OperateUser.findById(voucher.operatorId);
            voucher.operatorName = "运营人员：" + operateUser.loginName;
        }
    }
}
