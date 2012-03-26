package models.resale;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import models.sales.Goods;
import play.db.jpa.Model;

@Entity
@Table(name = "resaler_fav")
public class ResalerFav extends Model {
    @ManyToOne
    public Resaler resaler;

    @ManyToOne
    public Goods goods;


    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;


    public ResalerFav(Resaler resaler, Goods goods) {
        this.resaler= resaler;
        this.goods = goods;
        this.lockVersion = 0;
        this.createdAt = new Date();
    }

    public static List<ResalerFav> findAll(Resaler resaler){
        return ResalerFav.find("byResaler", resaler).fetch();
    }

    /**
     * 加入或修改分销库列表
     *
     * @param resaler   用户
     * @param goods     商品
     */

    public static ResalerFav order(Resaler resaler, Goods goods) {
        if (resaler == null  || goods == null) {
            return null;
        }

        ResalerFav fav = ResalerFav.find("byResalerAndGoods", resaler, goods).first();

        //如果记录已存在，则更新记录，否则新建购物车记录
        if (fav!= null) {
            return fav;
        } else {
            return new ResalerFav(resaler, goods).save();
        }

    }

    /**
     * 从分销库中删除指定商品列表
     *
     * @param resaler     用户
     * @param goodsIds 商品列表，若未指定，则删除该用户所有的购物车条目
     * @return 成功删除的数量
     */
    public static int delete(Resaler resaler, List<Long> goodsIds) {
        if (resaler == null || goodsIds == null || goodsIds.size() == 0) {
            return 0;
        }

        Query query = play.db.jpa.JPA.em().createQuery(
                "select r from ResalerFav r where r.resaler = :resaler and r.goods.id in :goods");
        query.setParameter("resaler", resaler);
        query.setParameter("goods", goodsIds);
        List<ResalerFav> favs = query.getResultList();

        for (ResalerFav fav : favs){
            fav.delete();
        }
        return favs.size();
    }

}
