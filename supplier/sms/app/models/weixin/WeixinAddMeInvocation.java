package models.weixin;

import util.extension.ExtensionResult;

/**
 * User: tanglq
 * Date: 13-3-25
 * Time: 下午5:21
 */
public class WeixinAddMeInvocation extends WeixinInvocation {
    @Override
    public ExtensionResult execute(WeixinContext context) {
        context.resultText = "欢迎使用【一百券商家助手】，使用【一百券商家助手】可通过微信进行消费券验证。请输入商家后台所提供的『身份识别码』，绑定您的微信。";
        //context.resultText = "谢谢使用【一百券商家助手】，我们期待能为您提供更好的服务！";
        return ExtensionResult.SUCCESS;
    }

    @Override
    public boolean match(WeixinContext context) {
        if (context.weixinRequest.msgType == WeixinMessageType.TEXT) {
            String content = context.weixinRequest.selectTextTrim("Content");
            return "Hello2BizUser".equals(content);   //加好友后会出现此信息.
        }
        return false;
    }
}
