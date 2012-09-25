package controllers;

import models.accounts.CashCoupon;
import models.admin.OperateUser;
import operate.rbac.annotations.ActiveNavigation;
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
@ActiveNavigation("cash_coupon_generator")
public class CashCouponGenerator extends Controller{
    private static final String DECIMAL_FORMAT = "00000";

    @ActiveNavigation("cash_coupon_generator")
    public static void index(){
        List<CashCoupon> coupons = CashCoupon.findAll();
        for(CashCoupon coupon: coupons) {
            if(coupon.operatorId != null) {
                OperateUser user = OperateUser.findById(coupon.operatorId);
                if (user != null) {
                    coupon.operatorName = user.loginName;
                }
            }
        }
        render(coupons);
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
        index();
    }
}
