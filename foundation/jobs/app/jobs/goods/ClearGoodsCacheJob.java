package jobs.goods;

import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.jobs.JobWithHistory;
import models.jobs.annotation.JobDefine;
import models.sales.Goods;
import models.sales.GoodsSchedule;
import models.sales.GoodsStatus;
import play.jobs.Every;
import util.DateHelper;

import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-11-19
 * Time: 上午11:22
 */
@JobDefine(title="更新商品缓存", description="查询三分钟内即将按上架或下架、排期的商品，清除商品缓存", retainHistoryMinutes = 1200)
@Every("1mn")
public class ClearGoodsCacheJob extends JobWithHistory {

    /**
     * 清除缓存
     */
    @Override
    public void doJobWithHistory() {
        //查询三分钟内即将按上架时间显示的商品，清除缓存
        List<Goods> onsaleGoodsList = getBeginSaleList();
        clearGoodsCache(onsaleGoodsList, "onsale");
        //查询三分钟内即将下架的商品，清除缓存，然后不显示在网站
        List<Goods> offSaleList = getOffSaleList();
        clearGoodsCache(offSaleList, "offsale");
        //清除排期商品的缓存
        List<GoodsSchedule> scheduleList = getScheduleGoodsList();
        clearGoodsScheduleCache(scheduleList);
    }

    private List<GoodsSchedule> getScheduleGoodsList() {
        String sql = "select g from GoodsSchedule g where g.effectiveAt>=:beginDate " +
                " order by g.id";
        Query query = GoodsSchedule.em().createQuery(sql);
        query.setParameter("beginDate", DateUtil.getBeginOfDay());
        query.setFirstResult(0);
        query.setMaxResults(200);
        return query.getResultList();

    }

    /**
     * 清除缓存
     *
     * @param goodsList
     */
    private void clearGoodsScheduleCache(List<GoodsSchedule> goodsList) {
        for (GoodsSchedule goods : goodsList) {
            CacheHelper.delete(GoodsSchedule.CACHEKEY);
            CacheHelper.delete(GoodsSchedule.CACHEKEY + goods.id);
        }
    }

    /**
     * 清除缓存
     *
     * @param goodsList
     */
    private void clearGoodsCache(List<Goods> goodsList, String status) {
        for (Goods goods : goodsList) {
            if (status.equals("offsale")) {
                goods.refresh();
                goods.status = GoodsStatus.OFFSALE;
                goods.save();
            }
            CacheHelper.delete(Goods.CACHEKEY);
            CacheHelper.delete(Goods.CACHEKEY + goods.id);
            CacheHelper.delete(Goods.CACHEKEY_SALECOUNT + goods.id);
            CacheHelper.delete(Goods.CACHEKEY_BASEID + goods.id);
        }
    }

    /**
     * 查询三分钟内即将按上架时间显示的商品
     *
     * @return
     */
    private List<Goods> getBeginSaleList() {
        String sql = "select g from Goods g where g.deleted=:deleted and g.status =:status and g.isHideOnsale = false " +
                "and g.beginOnSaleAt >:beginDate and g.beginOnSaleAt <=:endDate and g.expireAt > :expireAt" +
                " order by g.id";
        Query query = Goods.em().createQuery(sql);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        query.setParameter("status", GoodsStatus.ONSALE);
        query.setParameter("beginDate", DateHelper.beforeMinuts(1));
        query.setParameter("endDate", DateHelper.afterMinuts(1));
        query.setParameter("expireAt", new Date());
        query.setFirstResult(0);
        query.setMaxResults(200);
        return query.getResultList();
    }

    /**
     * 查询三分钟内即将下架的抽奖商品，
     *
     * @return
     */
    private List<Goods> getOffSaleList() {
        String sql = "select g from Goods g where g.deleted=:deleted and g.status =:status and g.isHideOnsale = false " +
                "and g.beginOnSaleAt <=:beginOnSaleAt and g.endOnSaleAt <=:endOnSaleAt" +
                " order by g.id";
        Query query = Goods.em().createQuery(sql);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        query.setParameter("status", GoodsStatus.ONSALE);
        query.setParameter("beginOnSaleAt", new Date());
        query.setParameter("endOnSaleAt", new Date());
        query.setFirstResult(0);
        query.setMaxResults(200);
        return query.getResultList();
    }
}
