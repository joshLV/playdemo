/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package controllers.sales;

//import org.apache.commons.codec.digest.DigestUtils;

import play.modules.sales.GoodsPlugin;
import play.mvc.Controller;

/**
 * 通用说明：
 *
 * @author yanjy
 * @version 1.0 02/8/12
 */
public class Goods extends Controller {

    /**
     * 展示添加商品页面
     */
    public static void index() {
        render();
    }

    /**
     * 添加商品
     */
    public static void addGoods(models.sales.Goods goods) {
        if (validation.hasErrors()) {
            error("Validation errors");
        }
        GoodsPlugin.addGoods(goods);
        index();
    }
}
