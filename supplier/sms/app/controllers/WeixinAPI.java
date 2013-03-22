package controllers;

import models.weixin.WeixinContext;
import models.weixin.WeixinInvocation;
import models.weixin.WeixinRequest;
import models.weixin.WeixinResponse;
import models.weixin.WeixinUtil;
import models.weixin.response.WeixinTextResponse;
import org.w3c.dom.Document;
import play.Logger;
import play.libs.IO;
import play.libs.XML;
import play.mvc.Controller;
import util.extension.DefaultAction;
import util.extension.ExtensionInvoker;
import util.extension.ExtensionResult;

/**
 * @author likang
 *         Date: 13-3-19
 */
public class WeixinAPI extends Controller {
    public static void heartbeat(String signature, String timestamp, String nonce, String echostr) {
        if (WeixinUtil.checkSignature(signature, timestamp, nonce)) {
            renderText(echostr);
        }
    }

    public static void message() {
        String restXml = IO.readContentAsString(request.body);
        Logger.info("restXML=%s", restXml);
        Document document = XML.getDocument(restXml);
        WeixinRequest message = WeixinUtil.parseMessage(document);

        WeixinResponse response;
        switch (message.msgType) {
            case TEXT:
                response = doText(message);
                break;
            default:
                Logger.info("Invalid mst Type.");
                renderText("invalid msg type");
                return;
        }

        render("weixin/" + response.getMsgType().toString().toLowerCase() + "Response.xml", response);
    }

    private static WeixinResponse doText(WeixinRequest weixinRequest) {
        WeixinTextResponse response = new WeixinTextResponse();
        response.toUserName = weixinRequest.fromUserName;

        WeixinContext weixinContext = WeixinContext.build(weixinRequest);
        ExtensionResult result = ExtensionInvoker.run(WeixinInvocation.class, weixinContext, new DefaultAction<WeixinContext>() {
            @Override
            public ExtensionResult execute(WeixinContext context) {
                context.resultText = "欢迎使用『一百券商家助手』，目前不能识别您的输入。请输入身份绑定码，如已经绑定，请输入券号进行券验证操作。";
                return ExtensionResult.SUCCESS;
            }
        });
        if (result.isOk()) {
            response.content = weixinContext.resultText;
        } else {
            response.content = "处理失败：" + result.message;
        }
        Logger.info("  response.content=%s", response.content);
        return response;
    }
}
