package models.job.dangdang.listener;

import com.uhuila.common.constants.DeletedStatus;
import models.dangdang.groupbuy.DDGroupBuyUtil;
import models.order.OuterOrderPartner;
import models.sales.GoodsStatus;
import models.sales.MaterialType;
import models.sales.ResalerProduct;
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

    @Override
    public void doJob() {
        if(Play.runingInTestMode()){
            return;
        }
        Logger.info("\n--------------Start dangdang syncSellCount job----");

        //取得dangdang分销商商品库中在售的商品（不包含抽奖商品）
        List<ResalerProduct> products = ResalerProduct.find("partner=? and goods.materialType = ? and " +
                "goods.deleted=? and goods.status=? and goods.expireAt >=? and goods.isLottery = false order by createdAt DESC",
                OuterOrderPartner.DD , MaterialType.ELECTRONIC, DeletedStatus.UN_DELETED, GoodsStatus.ONSALE, new Date()).fetch();
        for (ResalerProduct product : products) {
            DDGroupBuyUtil.syncSellCount(product);
        }

        Logger.info("\n--------------End dangdang syncSellCount job.");
    }
}
