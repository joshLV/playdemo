package play.modules.sales;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses;

/**
 * 商品模块的play插件.
 * <p/>
 * User: sujie
 * Date: 2/9/12
 * Time: 11:06 AM
 */
public class GoodsPlugin extends PlayPlugin {
    private static boolean pluginActive = false;
    private static GoodsService service;

    /**
     * 应用启动时启动服务.
     */
    public void onApplicationStart() {
        ApplicationClasses.ApplicationClass goodsService = Play.classes.getAssignableClasses(GoodsService.class).get(0);

        if (goodsService == null) {
            Logger.error("Goods plugin disabled. No class implements GoodsService interface");
        } else {
            try {
                service = (GoodsService) goodsService.javaClass.newInstance();
                pluginActive = true;
            } catch (Exception e) {
                Logger.error(e, "Registration plugin disabled. Error when creating new instance");
            }
        }
    }

    public static void addGoods(Object goods) {
        if (pluginActive) {
            service.addGoods(goods);
        }
    }
}
