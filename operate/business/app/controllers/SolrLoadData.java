package controllers;

import operate.rbac.annotations.ActiveNavigation;
import play.modules.solr.Solr;
import play.mvc.Controller;
import play.mvc.With;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-1
 * Time: 下午3:16
 */
@With(OperateRbac.class)
@ActiveNavigation("solr_load")
public class SolrLoadData extends Controller {

    @ActiveNavigation("solr_load")
    public static void index() {
        render();
    }

    /**
     * 初始化solr
     */
    public static void initSolr() {
        try {
            Solr.indexAll();
        } catch (Exception e) {
            renderText("err");
        }
        renderText("ok");
    }
}
