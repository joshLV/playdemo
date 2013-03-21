package controllers;

import models.weixin.WeixinRequest;
import models.weixin.WeixinResponse;
import models.weixin.WeixinUtil;
import models.weixin.response.WeixinTextResponse;
import org.w3c.dom.Document;
import play.libs.IO;
import play.libs.WS;
import play.libs.XML;
import play.mvc.Controller;

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
        Document document = XML.getDocument(restXml);
        WeixinRequest message = WeixinUtil.parseMessage(document);
        WS.url("").put();

        WeixinResponse response;
        switch (message.msgType) {
            case TEXT:
                response = doText(message);
                break;
            default:
                renderText("invalid msg type");
                return;
        }

        render("weixin/" + response.getMsgType().toString().toLowerCase() + "Response.xml", response);
    }

    private static WeixinResponse doText(WeixinRequest message) {
        WeixinTextResponse response = new WeixinTextResponse();
        response.toUserName = message.fromUserName;
        response.content = message.selectTextTrim("Content");
        return response;
    }
}
