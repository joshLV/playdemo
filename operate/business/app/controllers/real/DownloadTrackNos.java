package controllers.real;

import controllers.OperateRbac;
import models.order.OuterOrderPartner;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 下载带运单号的跟踪表文件，供上传到不同.
 * User: tanglq
 * Date: 13-3-12
 * Time: 下午5:54
 */
@With(OperateRbac.class)
@ActiveNavigation("download_track_no_for_resaler")
public class DownloadTrackNos extends Controller {

    /**
     * 选择一个渠道，然后出现下载列表.
     * @param partner
     */
    public static void index(OuterOrderPartner partner) {
        if (partner == null) {
            //默认京东
            partner = OuterOrderPartner.JD;
        }
        render(partner);
    }

    /**
     * 通过下载链接下载对应的excel文件.
     * @param partner
     * @param outGoodsId
     */
    public static void download(OuterOrderPartner partner, Long outGoodsId) {

    }

}
