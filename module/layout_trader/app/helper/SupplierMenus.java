package helper;

import cache.CacheCallBack;
import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import models.sales.Goods;
import play.mvc.Controller;

import java.util.List;

/**
 * User: yan
 * Date: 13-6-13
 * Time: 上午11:51
 */
public class SupplierMenus extends Controller {
    public static boolean getAppointmentMenu(final Long supplierId) {
        List<Goods> secondaryVerifyGoodsList = CacheHelper.getCache(CacheHelper.getCacheKey(
                new String[]{models.sales.Goods.CACHEKEY, String.valueOf(supplierId)}, "SECONDARY_VERIFY_GOODS"),
                new CacheCallBack<List<Goods>>() {
                    @Override
                    public List<models.sales.Goods> loadData() {
                        return models.sales.Goods.find(" select g from Goods g where g.supplierId=? and g.deleted=? and g.id in (select gp.goodsId from GoodsProperty gp where gp.name=? and gp.value=1) ",
                                supplierId, DeletedStatus.UN_DELETED, Goods.SECONDARY_VERIFICATION).fetch();
                    }
                });
        boolean isSecondaryVerificationGoods = false;
        if (secondaryVerifyGoodsList != null && secondaryVerifyGoodsList.size() > 0) {
            isSecondaryVerificationGoods = true;
        }
        return isSecondaryVerificationGoods;
    }
}
