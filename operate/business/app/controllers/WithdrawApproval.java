package controllers;

import models.accounts.WithdrawBill;
import models.accounts.WithdrawBillStatus;
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
public class WithdrawApproval extends Controller {

    public static void index(){
        List<WithdrawBill> withdrawBills = WithdrawBill.find("order by appliedAt desc").fetch();
        render(withdrawBills);
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
        index();
    }
}
