package controllers;

import models.dangdang.groupbuy.DDGroupBuyUtil;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.mvc.Controller;

import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-13
 * Time: 下午5:02
 */
public class DDSendMessageAPI extends Controller {
    private static final String VER = Play.configuration.getProperty("dangdang.version");
    private static final String SPID = Play.configuration.getProperty("dangdang.spid", "3000003");
    private static final String API_NAME = "send_msg";

    public static void sendMessage() {
        Logger.info("[DDSendMessageAPI] begin ");
        //取得参数信息 必填信息
        Map<String, String> params = DDGroupBuyUtil.filterPlayParams(request.params.allSimple());
        //取得参数信息
        String sign = StringUtils.trimToEmpty(params.get("sign")).toLowerCase();
        String data = StringUtils.trimToEmpty(params.get("data"));
        String time = StringUtils.trimToEmpty(params.get("call_time"));
        String verifySign = DDGroupBuyUtil.sign(API_NAME, data, time);
        if (StringUtils.isBlank(sign) || !sign.equals(verifySign)) {
            Logger.info("[DDSendMessageAPI] sign failed ");
            /*
            Response response = new Response();
            response.spid = SPID;
            response.ver = VER;
            response.errorCode = DDErrorCode.VERIFY_FAILED;
            response.desc = "sign验证失败！";
            render(response);
            */
        }

        //当当调用接口
        /*
        Response response = DDAPIUtil.sendSMS(data);
        Logger.info("[DDSendMessageAPI] end!" + response.desc);
        render(response);
        */
    }
}
