package controllers;

import models.consumer.*;
import models.accounts.*;
import models.order.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import play.data.binding.As;

import java.util.*;

import controllers.modules.cas.SecureCAS;

//@With(SecureCAS.class)
public class PaymentInfo extends Controller {

    public static void index(long id) {
        String username = session.get("username");
        username = "likang";
        User user = User.find("byLoginName", username).first();
        Accounts account = Accounts.find("byUid",user.getId()).first();

        models.order.Orders order = models.order.Orders.findById(id);

        long goodsNumber = 0L;
        if (order != null){
            Object result = OrderItems.em().createNativeQuery("select sum(number) from order_items where order_id =" + order.getId()).getSingleResult();
            if (result != null) goodsNumber = (Long)result;
        }

        render(user, account, order, goodsNumber);
    }

    public static void confirm(long orderId, boolean useBalance) {
        String username = session.get("username");
        username = "likang";
        User user = User.find("byLoginName", username).first();
        Accounts account = Accounts.find("byUid",user.getId()).first();
        models.order.Orders order = models.order.Orders.findById(orderId);

        if (order == null){
            error(500,"no such order");
            return;
        }

        ok();
    }
}

