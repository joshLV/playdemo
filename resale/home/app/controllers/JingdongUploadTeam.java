package controllers;

import models.jingdong.JDGroupBuyUtil;
import models.jingdong.groupbuy.response.CityResponse;
import play.mvc.Controller;

import java.util.List;

/**
 * @author likang
 *         Date: 12-10-16
 */
public class JingdongUploadTeam extends Controller{
    public static void prepare(Long goodsId){
        models.sales.Goods goods = new models.sales.Goods();
        List<CityResponse> cities = JDGroupBuyUtil.queryCity();

        render(goods, cities);
    }

    public static void upload(){

    }
}
