package controllers.supplier;

import controllers.OperateRbac;
import jobs.dadong.DadongProductsSyncJob;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 大东票务 产品同步。
 * User: tanglq
 * Date: 13-1-18
 * Time: 下午4:46
 */
@With(OperateRbac.class)
public class DadangProducts extends Controller {

    @ActiveNavigation("suppliers_index")
    public static void sync() {
        Supplier dadong = Supplier.findByDomainName("dadang");

        DadongProductsSyncJob job = new DadongProductsSyncJob();
        F.Promise<Integer> promise = job.now();

        Integer newCount = await(promise);
        render(newCount);
    }
}
