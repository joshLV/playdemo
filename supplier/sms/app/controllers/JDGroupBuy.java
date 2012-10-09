package controllers;

import models.jingdong.groupbuy.JDRequest;
import models.jingdong.groupbuy.SendOrder;
import models.order.Order;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * @author likang
 * Date: 12-9-28
 */
public class JDGroupBuy extends Controller{
    @Before
    public static void baseResponse(){
        renderArgs.put("version", "1.0");
        renderArgs.put("venderId", "our venderid");//todo
        renderArgs.put("encrypt", "true");
        renderArgs.put("zip","false");
    }

    public static void sendOrder(){
        String restXml = request.body.toString();
        JDRequest<SendOrder> sendOrderJDRequest = new JDRequest<>();
        sendOrderJDRequest.parse(restXml, new SendOrder());
        SendOrder sendOrder = sendOrderJDRequest.data;


        renderArgs.put("resultCode", "400");
        renderArgs.put("resultMessage", "success");
        renderTemplate("JDGroupBuy/sendOrder.xml");
    }

    public static void sendSms(){
        String restXml = request.body.toString();
    }

    private static void generateYibaiquanOrder(SendOrder sendOrder) {
        Order ybqOrder = new Order();

    }
}
