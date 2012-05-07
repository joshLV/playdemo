package controllers;

import models.accounts.WithdrawBill;
import models.accounts.WithdrawBillStatus;
import play.mvc.Controller;
import play.mvc.With;

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

    public static void approval(Long id, String action){
        WithdrawBill bill = WithdrawBill.findById(id);
        if(bill == null || bill.status != WithdrawBillStatus.APPLIED){
            error("cannot find the withdraw bill or the bill is processed");
            return;
        }
        if (action.equals("YES")){
            bill.success("无");
        }else if(action.equals("NO")){
            bill.rejected("无");
        }
        index();
    }
}
