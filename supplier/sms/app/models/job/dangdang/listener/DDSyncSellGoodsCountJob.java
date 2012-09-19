package models.job.dangdang.listener;

import com.uhuila.common.constants.DeletedStatus;
import models.dangdang.DDAPIInvokeException;
import models.dangdang.DDAPIUtil;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.resale.ResalerStatus;
import models.sales.GoodsStatus;
import models.sales.MaterialType;
import play.Logger;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;

import java.util.Date;
import java.util.List;

/**
 * <p/>
 * 每三小时更新同步当当的商品售出数量
 * User: yanjy
 * Date: 12-9-13
 * Time: 下午2:31
 */
@Every("3h")
public class DDSyncSellGoodsCountJob extends Job {
    public static String DD_LOGIN_NAME = Play.configuration.getProperty("dangdang.resaler_login_name", "dangdang");

    @Override
    public void doJob() {
        Logger.info("start syncSellCount job");
        //定位请求者
        Resaler resaler = Resaler.find("loginName=? and status=?", DD_LOGIN_NAME, ResalerStatus.APPROVED).first();
        if (resaler == null) {
            Logger.error("dangdang resaler is not existed!");
            return;
        }
        //取得dangdang分销商商品库中在售的商品（不包含抽奖商品）
        List<ResalerFav> favs = ResalerFav.find("resaler=? and goods.materialType = ? and " +
                "goods.deleted=? and goods.status=? and goods.expireAt >=? and goods.isLottery = false order by createdAt DESC", resaler,
                MaterialType.ELECTRONIC, DeletedStatus.UN_DELETED, GoodsStatus.ONSALE, new Date()).fetch();
        if (favs.size() == 0) {
            Logger.error("dangdang resaler no library goods!");
            return;
        }
        for (ResalerFav resalerGoods : favs) {
            try {
                //更新当当商品的售出数量
                DDAPIUtil.syncSellCount(resalerGoods.goods);
            } catch (DDAPIInvokeException e) {
                //调用出错后打印错误日志
                Logger.error(e.getMessage());
            }
        }
    }
}
