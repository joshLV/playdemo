package controllers;

import java.math.BigDecimal;

import models.accounts.AccountType;
import models.order.ChargeOrder;
import models.resale.Resaler;

import controllers.modules.resale.cas.SecureCAS;

import play.mvc.Controller;
import play.mvc.With;

@With(SecureCAS.class)
public class Charge extends Controller{
    
    public static void index(){
        render();
    }
    public static void create(BigDecimal amount){
        Resaler resaler = SecureCAS.getResaler();
        if(amount.compareTo(BigDecimal.ONE) <= 0){
            error("invalid charge amount");
        }
        ChargeOrder chargeOrder= new ChargeOrder(resaler.getId(), AccountType.RESALER, amount);
        chargeOrder.save();
        redirect("/charge_payment_info/" + chargeOrder.getId());
    }

}
