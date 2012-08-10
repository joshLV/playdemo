package controllers;

import play.mvc.Controller;
import models.sales.PointGoods;

/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-9
 * Time: 下午1:14
 * To change this template use File | Settings | File Templates.
 */
public class PointGoodsOrder extends Controller {

    public static void index(Long pointGoodsId){

        // 商品ID 为空
        if (pointGoodsId == null) {
            error("no goods specified");
            return;
        }

        PointGoods pointGoods = PointGoods.findById(pointGoodsId);
        System.out.println("Name --------- " + pointGoods.name);

    }
}
