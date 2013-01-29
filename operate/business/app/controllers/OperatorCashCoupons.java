package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.accounts.CashCoupon;
import models.accounts.CashCouponCondition;
import models.admin.OperateUser;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.regex.Pattern;

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
        if("delete".equals(request.params.get("action"))){
            String deleteIdStr = request.params.get("delete_id");
            if(!StringUtils.isBlank(deleteIdStr)){
                CashCoupon coupon = CashCoupon.findById(Long.parseLong(deleteIdStr));
                if (coupon != null){
                    coupon.deleted = DeletedStatus.DELETED;
                    coupon.save();
                    renderArgs.put("page", pageNumber);
                    index(null);
                }
            }
        }


        JPAExtPaginator<CashCoupon> couponPage = CashCoupon.findByCondition(condition,
                pageNumber, PAGE_SIZE);

        for(CashCoupon coupon: couponPage.getCurrentPage()) {
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
    public static void generator(String err){
        render(err);
    }

    @ActiveNavigation("cash_coupon_generator")
    public static void generate(BigDecimal faceValue, int count, String name, String prefix){
        Pattern pattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{0,9}$");
        if(name == null || name.trim().equals("")){
            generator("券名称不能为空");
        }else if ( prefix == null || !pattern.matcher(prefix).matches()){
            generator("卡号前缀不符合规范");
        }else if (faceValue == null
                || faceValue.compareTo(BigDecimal.ONE) < 0
                || faceValue.compareTo(new BigDecimal("10000")) > 0
                || faceValue.setScale(2, RoundingMode.FLOOR).compareTo(faceValue) != 0){
            generator("面值不符合要求");
        }else if(count < 1 || count > 9999 ){
            generator("数量不符合要求");
        }else {
            CashCoupon coupon = CashCoupon.find("bySerialNo",
                    prefix + new DecimalFormat("00000").format(1)).first();
            if (coupon != null){
                generator("卡号前缀已存在");
            }
        }
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
            cashCoupon.serialNo = prefix + decimalFormat.format(i + 1);
            cashCoupon.operatorId = OperateRbac.currentUser().getId();
            cashCoupon.save();
        }
        index(null);
    }
}
