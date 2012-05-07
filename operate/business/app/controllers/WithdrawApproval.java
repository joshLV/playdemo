package controllers;

import models.accounts.WithdrawBill;
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

    public static void approval(String action){

    }
}
