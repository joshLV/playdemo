package controllers;

import models.yihaodian.YHDUtil;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.With;

import java.util.HashMap;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-9-10
 */
@With(OperateRbac.class)
@ActiveNavigation("yihaodian_api_index")
public class YihaodianAPI extends Controller{
    public static void index(){
        render();
    }

    public static void request(String method, String paramStr){
        Map<String, String> params = new HashMap<>();
        String[] lines = paramStr.split("\n");
        for(String line : lines){
            String[] param = line.split("::");
            params.put(param[0], param[1]);
        }
        if (StringUtils.isNotBlank(method))
            renderText(YHDUtil.sendRequest(params, method));
    }
}
