package controllers.supplier;

import controllers.OperateRbac;
import jobs.dadong.DadongProductsSyncRequest;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 大东票务 产品同步。
 * User: tanglq
 * Date: 13-1-18
 * Time: 下午4:46
 */
@With(OperateRbac.class)
public class DadongProducts extends Controller {

    @ActiveNavigation("suppliers_index")
    public static void sync() {
        Integer newCount = DadongProductsSyncRequest.syncProducts();
        render(newCount);
    }

}
