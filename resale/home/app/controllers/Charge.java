package controllers;

import java.math.BigDecimal;

import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.util.AccountUtil;
import models.order.ChargeOrder;
import models.order.Order;
import models.resale.Resaler;

import controllers.modules.resale.cas.SecureCAS;

import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

@With(SecureCAS.class)
public class Charge extends Controller{
    
    public static void index(){
        render();
    }
    public static void create(BigDecimal amount){
        Resaler resaler = SecureCAS.getResaler();
        if(amount == null || amount.compareTo(BigDecimal.ONE) < 0){
            Validation.addError("charge.amount", "最少充值1.00元!");
            Validation.keep();
            index();
        }

        Order order = Order.createChargeOrder(resaler.getId(), AccountType.RESALER );
        order.amount = amount;
        order.needPay = amount;
        order.save();
        redirect("/payment_info/" + order.getId());
    }

}
