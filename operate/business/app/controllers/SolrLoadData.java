package controllers;

import operate.rbac.annotations.ActiveNavigation;
import play.modules.solr.Solr;
import play.mvc.Controller;
import play.mvc.With;
import org.apache.commons.lang.StringUtils;

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
        String deleteFlag = request.params.get("delete");
        try {
            //是否增量加载的标志
            if (StringUtils.isBlank(deleteFlag)) {
                Solr.deleteAll();
            }
            Solr.indexAll();
        } catch (Exception e) {
            renderText(e.fillInStackTrace());
        }
        renderText("ok");
    }
}
