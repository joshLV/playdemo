package controllers;

import models.accounts.*;
import models.consumer.User;
import models.resale.Resaler;
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
 * Date: 12-5-7
 */

@With(OperateRbac.class)
@ActiveNavigation("withdraw_approval_index")
public class WithdrawApproval extends Controller {
    private static final int PAGE_SIZE = 20;

    public static void index(WithdrawBillCondition condition){
        String page = request.params.get("page");
        int pageNumber =  StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        if(condition == null){
            condition = new WithdrawBillCondition();
        }

        JPAExtPaginator<WithdrawBill> billPage = WithdrawBill.findByCondition(condition,
                pageNumber, PAGE_SIZE);

        render(billPage, condition);
    }

    public static void detail(Long id){
        WithdrawBill bill = WithdrawBill.findById(id);
        if (bill == null){
            error("withdraw bill not found");
        }
        render(bill);
    }

    public static void approve(Long id, String action, BigDecimal fee, String comment){
        WithdrawBill bill = WithdrawBill.findById(id);
        if(bill == null || bill.status != WithdrawBillStatus.APPLIED){
            error("cannot find the withdraw bill or the bill is processed");
            return;
        }
        if (action.equals("agree")){
            if(fee == null || fee.compareTo(BigDecimal.ZERO) < 0){
                error("invalid fee");
                return;
            }
            bill.agree(fee, comment);
        }else if(action.equals("reject")){
            bill.reject(comment);
        }
        index(null);
    }
}
