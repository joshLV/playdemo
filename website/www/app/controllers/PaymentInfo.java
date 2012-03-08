package controllers;

import controllers.modules.webcas.WebCAS;
import models.accounts.util.AccountUtil;
import models.accounts.util.TradeUtil;
import models.consumer.*;
import models.accounts.*;
import models.order.*;
import play.mvc.Controller;
import play.mvc.With;

import thirdpart.alipay.services.*;

import java.math.BigDecimal;
import java.util.*;

import controllers.modules.cas.SecureCAS;

@With({SecureCAS.class, WebCAS.class})
public class PaymentInfo extends Controller {

    /**
     * 展示确认支付信息页.
     *
     * @param id 订单ID
     */
    public static void index(long id) {
        //加载用户账户信息
        User user = WebCAS.getUser();
        Account account = Account.find("byUid", user.getId()).first();

        //加载订单信息
        models.order.Orders order = models.order.Orders.find("byIdAndUser",id, user).first();
        long goodsNumber = models.order.Orders.itemsNumber(order);
        
        List<PaymentSource> paymentSources = PaymentSource.find("order by order desc").fetch();

        render(user, account, order, goodsNumber, paymentSources);
    }


    /**
     * 接收用户反馈的订单的支付信息.
     *
     * @param orderId           订单ID
     * @param useBalance        是否使用余额
     * @param paymentSourceCode 网银代码
     */
    public static void confirm(long orderId, boolean useBalance, String paymentSourceCode) {
        User user = WebCAS.getUser();
        models.order.Orders order = models.order.Orders.find("byIdAndUser",orderId,user).first();
        
        if (order == null){
            error(500,"no such order");
            return;
        }

        Account account = Account.find("byUid", user.getId()).first();

        //计算使用余额支付和使用银行卡支付的金额
        BigDecimal balancePaymentAmount = BigDecimal.ZERO;
        BigDecimal ebankPaymentAmount = BigDecimal.ZERO;
        if (useBalance){
            balancePaymentAmount = account.amount.min(order.needPay);
            ebankPaymentAmount = order.needPay.subtract(balancePaymentAmount);
        }else {
            ebankPaymentAmount = order.needPay;
        }
        order.accountPay = balancePaymentAmount;
        order.discountPay = ebankPaymentAmount;

        //创建订单交易
        PaymentSource paymentSource = PaymentSource.find("byCode", paymentSourceCode).first();
        TradeBill tradeBill =
                TradeUtil.createOrderTrade(account, balancePaymentAmount,ebankPaymentAmount,paymentSource, orderId);
        if(tradeBill == null){
            error(500, "error create trade bill");
            return;
        }
        order.payRequestId = tradeBill.getId();
        order.paymentSourceCode = paymentSourceCode;

        //如果使用余额足以支付，则付款直接成功
        if (ebankPaymentAmount.compareTo(BigDecimal.ZERO) == 0){
            order.status = OrderStatus.PAID;
            TradeUtil.success(tradeBill);
            order.save();
            return;
        }

        /*网银付款*/

        //无法确定支付渠道
        if(paymentSource == null){
            error(500, "can not get paymentSource");
            return;
        }

        order.save();
        render(order);

    }

    /**
     * 生成网银跳转页.
     *
     * @param orderId               订单
     */
    public static void payIt(long orderId){
        User user = WebCAS.getUser();
        models.order.Orders order = models.order.Orders.find("byIdAndUser",orderId,user).first();

        if (order == null){
            error(500,"no such order");
            return;
        }

        //必填参数//

        //请与贵网站订单系统中的唯一订单号匹配
        String out_trade_no = order.orderNumber;
        //订单名称，显示在支付宝收银台里的“商品名称”里，显示在支付宝的交易管理的
        //“商品名称”的列表里。
        String subject = "优惠啦测试商品";
        //订单描述、订单详细、订单备注，显示在支付宝收银台里的“商品描述”里
        String body = "优惠啦测试商品描述";
        //订单总金额，显示在支付宝收银台里的“应付总额”里
        String total_fee = order.needPay.toString();


        //扩展功能参数——默认支付方式//

        //默认支付方式，取值见“即时到帐接口”技术文档中的请求参数列表
        String paymethod = "directPay";
        //默认网银代号，代号列表见“即时到帐接口”技术文档“附录”→“银行列表”
        String defaultbank = "";

        //扩展功能参数——防钓鱼//

        //防钓鱼时间戳
//        String anti_phishing_key  = AlipayService.query_timestamp();

        //获取客户端的IP地址，建议：编写获取客户端IP地址的程序
//        String exter_invoke_ip= "127.0.0.1";//request.getRemoteAddr();
        //注意：
        //1.请慎重选择是否开启防钓鱼功能
        //2.exter_invoke_ip、anti_phishing_key一旦被设置过，
        //那么它们就会成为必填参数
        //3.开启防钓鱼功能后，服务器、本机电脑必须支持远程XML解析，
        //请配置好该环境。
        //4.建议使用POST方式请求数据
        //示例：
        //anti_phishing_key = AlipayService.query_timestamp();
        //获取防钓鱼时间戳函数
        //exter_invoke_ip = "202.1.1.1";

        //扩展功能参数——其他///

        //自定义参数，可存放任何内容（除=、&等特殊字符外），不会显示在页面上
        String extra_common_param = "";
        //默认买家支付宝账号
        String buyer_email = "";
        //商品展示地址，要用http:// 格式的完整路径，不允许加?id=123这类自定义参数
        String show_url = "http://test.uhuila.com:19001/";
        //扩展功能参数——分润(若要使用，请按照注释要求的格式赋值)//

        //提成类型，该值为固定值：10，不需要修改
        String royalty_type = "";
        //提成信息集
        String royalty_parameters ="";
        //注意：
        //与需要结合商户网站自身情况动态获取每笔交易的各分润收款账号、各分润金额、各分润说明。最多只能设置10条
        //各分润金额的总和须小于等于total_fee
        //提成信息集格式为：收款方Email_1^金额1^备注1|收款方Email_2^金额2^备注2
        //示例：
        //royalty_type = "10"
        //royalty_parameters    = "111@126.com^0.01^分润备注一|222@126.com^0.01^分润备注二"

        //////////////////////////////////////////////////////////////////////////////////

        //把请求参数打包成数组
        Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("payment_type", "1");
        sParaTemp.put("out_trade_no", out_trade_no);
        sParaTemp.put("subject", subject);
//        sParaTemp.put("body", body);    //play 无法方便的获得传回的body参数，因此干脆不请求此参数
        sParaTemp.put("total_fee", total_fee);
        sParaTemp.put("show_url", show_url);
        sParaTemp.put("paymethod", paymethod);
        sParaTemp.put("defaultbank", defaultbank);
//        sParaTemp.put("anti_phishing_key", anti_phishing_key);
//        sParaTemp.put("exter_invoke_ip", exter_invoke_ip);
        sParaTemp.put("extra_common_param", extra_common_param);
        sParaTemp.put("buyer_email", buyer_email);
        sParaTemp.put("royalty_type", royalty_type);
        sParaTemp.put("royalty_parameters", royalty_parameters);

        //构造函数，生成请求URL
        String sHtmlText = AlipayService.create_direct_pay_by_user(sParaTemp);
        render(sHtmlText);

    }


}

