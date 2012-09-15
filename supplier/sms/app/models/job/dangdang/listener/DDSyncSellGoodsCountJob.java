package models.job.dangdang.listener;

import com.uhuila.common.constants.DeletedStatus;
import dangdang.DangDangApiUtil;
import models.sales.Goods;
import models.sales.GoodsStatus;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

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
    @Override
    public void doJob() {
        Logger.info("start syncSellCount job");
        //取得我们商品表中在售的商品（不包含抽奖商品）
        List<Goods> goodsList = Goods.find("deleted=? and status=? and isLottery=false",
                DeletedStatus.UN_DELETED, GoodsStatus.ONSALE).fetch();
        for (Goods goods : goodsList) {
            //更新当当商品的售出数量
            DangDangApiUtil.syncSellCount(goods);
        }
    }
}
