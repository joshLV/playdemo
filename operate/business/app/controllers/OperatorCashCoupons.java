package controllers;

import models.accounts.CashCoupon;
import models.accounts.CashCouponCondition;
import models.admin.OperateUser;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

/**
 * @author likang
 *         Date: 12-9-25
 */
@With(OperateRbac.class)
@ActiveNavigation("cash_coupon")
public class OperatorCashCoupons extends Controller{
    private static final String DECIMAL_FORMAT = "00000";
    private static final int PAGE_SIZE = 20;

    @ActiveNavigation("cash_coupon_index")
    public static void index(CashCouponCondition condition){
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null){
            condition = new CashCouponCondition();
        }
        JPAExtPaginator<CashCoupon> couponPage = CashCoupon.findByCondition(condition,
                pageNumber, PAGE_SIZE);

        for(CashCoupon coupon: couponPage) {
            if(coupon.operatorId != null) {
                OperateUser user = OperateUser.findById(coupon.operatorId);
                if (user != null) {
                    coupon.operatorName = user.loginName;
                }
            }
        }
        render(couponPage, condition);
    }

    @ActiveNavigation(("cash_coupon_generator"))
    public static void generator(){
        render();
    }

    @ActiveNavigation("cash_coupon_generator")
    public static void generate(BigDecimal faceValue, int count, String name, String prefix){
        Random random = new Random();
        DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT);

        for(int i = 0; i < count; i ++){
            String chargeCode = decimalFormat.format(random.nextInt(100000)) +
                    decimalFormat.format(random.nextInt(100000)) +
                    decimalFormat.format(random.nextInt(100000));

            CashCoupon cashCoupon = new CashCoupon();
            cashCoupon.chargeCode = chargeCode;
            cashCoupon.faceValue = faceValue;
            cashCoupon.name = name;
            cashCoupon.serialNo = prefix + decimalFormat.format(i);
            cashCoupon.operatorId = OperateRbac.currentUser().getId();
            cashCoupon.save();
        }
        index(null);
    }
}
