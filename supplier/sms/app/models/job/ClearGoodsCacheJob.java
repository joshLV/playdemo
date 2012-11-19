package models.job;

import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import models.sales.Goods;
import models.sales.GoodsStatus;
import play.jobs.Every;
import play.jobs.Job;
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
@Every("1mn")
public class ClearGoodsCacheJob extends Job {

    /**
     * 清除缓存
     */
    @Override
    public void doJob() {
        //查询三分钟内即将按上架时间显示的商品，清除缓存
        List<Goods> onsaleGoodsList = getBeginSaleList();
        clearGoodsCache(onsaleGoodsList);
        //查询三分钟内即将下架的商品，清除缓存，然后不显示在网站
        List<Goods> offSaleList = getOffSaleList();
        clearGoodsCache(offSaleList);
    }

    /**
     * 清除缓存
     *
     * @param goodsList
     */
    private void clearGoodsCache(List<Goods> goodsList) {
        for (Goods goods : goodsList) {
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
                "and g.beginOnSaleAt <=:beginOnSaleAt and g.expireAt > :beginDate and g.expireAt <= :endDate" +
                " order by g.id";
        Query query = Goods.em().createQuery(sql);
        query.setParameter("deleted", DeletedStatus.UN_DELETED);
        query.setParameter("status", GoodsStatus.ONSALE);
        query.setParameter("beginOnSaleAt", new Date());
        query.setParameter("beginDate", DateHelper.beforeMinuts(1));
        query.setParameter("endDate", DateHelper.afterMinuts(1));
        query.setFirstResult(0);
        query.setMaxResults(200);
        return query.getResultList();
    }
}
