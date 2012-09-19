package controllers;

import models.dangdang.DDAPIInvokeException;
import models.dangdang.DDAPIUtil;
import models.dangdang.ErrorCode;
import models.dangdang.Response;
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
        //取得参数信息 必填信息
        Map<String, String> params = DDAPIUtil.filterPlayParameter(request.params.all());
        //取得参数信息
        String sign = params.get("sign");
        String data = params.get("data");
        String time = params.get("time");
        if (sign.equals(DDAPIUtil.getSign(data, time, API_NAME))) {
            //当当调用接口
            Response response;
            try {
                response = DDAPIUtil.sendSMS(data);
            } catch (DDAPIInvokeException e) {
                response = new Response();
                response.spid = SPID;
                response.ver = VER;
                response.errorCode = ErrorCode.PARSE_XML_FAILED;
                response.desc = "解析请求参数失败！";
            }
            render(response);
        } else {
            Response response = new Response();
            response.spid = SPID;
            response.ver = VER;
            response.errorCode = ErrorCode.VERIFY_FAILED;
            response.desc = "验证sign失败！";
            render(response);
        }
    }

}
