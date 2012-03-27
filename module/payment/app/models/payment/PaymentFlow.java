package models.payment;

import java.util.Map;

import models.order.Order;

/**
 * @author likang
 *         Date: 12-3-16
 */
public interface PaymentFlow {
    
    public String generateForm(Order order);  //生成form表单
    public boolean verifyParams(Map<String, String[]> params); //校验返回参数
    public boolean paymentNotify(Map<String ,String[]> params);   //处理服务器回调
}
