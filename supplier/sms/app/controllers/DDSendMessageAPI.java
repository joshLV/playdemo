package controllers;

import models.dangdang.DangDangApiUtil;
import play.mvc.Controller;

import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-13
 * Time: 下午5:02
 */
public class DDSendMessageAPI extends Controller {

    public static void sendMessage() {
        Map<String, String[]> params = request.params.all();
        //取得参数信息
        String sign = params.get("sign")[0] == null ? "" : params.get("sign")[0].toString();
        String data = params.get("data")[0] == null ? "" : params.get("data")[0].toString();
        String time = params.get("time")[0] == null ? "" : params.get("time")[0].toString();
        String apiName = params.get("apiName")[0] == null ? "" : params.get("apiName")[0].toString();
        if (sign.equals(DangDangApiUtil.getSign(data, time, apiName))) {
            //当当调用接口
            String xml = DangDangApiUtil.sendSMS(data);
            renderXml(xml);
        } else {
            renderXml("");
        }
    }

}
