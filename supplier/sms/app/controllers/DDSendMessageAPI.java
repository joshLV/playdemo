package controllers;

import models.dangdang.DDAPIUtil;
import models.dangdang.ErrorCode;
import models.dangdang.Response;
import play.mvc.Controller;

import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-13
 * Time: 下午5:02
 */
public class DDSendMessageAPI extends Controller {
    public static String SPID = "";//todo
    public static String VER = "1.0";//todo

    public static void sendMessage() {
        Map<String, String[]> params = request.params.all();
        //取得参数信息
        String sign = params.get("sign")[0] == null ? "" : params.get("sign")[0].toString();
        String data = params.get("data")[0] == null ? "" : params.get("data")[0].toString();
        String time = params.get("time")[0] == null ? "" : params.get("time")[0].toString();
        String apiName = params.get("apiName")[0] == null ? "" : params.get("apiName")[0].toString();
        if (sign.equals(DDAPIUtil.getSign(data, time, apiName))) {
            //当当调用接口
            Response response = DDAPIUtil.sendSMS(data);
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
