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

        Long goodsNumber = (Long)OrderItems.em().createNativeQuery("select sum(number) from order_items where order_id =" + order.getId()).getSingleResult();
        if (goodsNumber ==null) goodsNumber = 0L;

        render(user, account, order, goodsNumber);
    }

    public static void useDiscount(long orderId, long discountId) {
        
    }
}

