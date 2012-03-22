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

import models.consumer.User;
import models.sales.Goods;
import models.sales.MaterialType;
import play.db.jpa.Model;

@Entity
@Table(name = "resaler_fav")
public class ResalerFav extends Model {
    @ManyToOne
    public User user;

    @ManyToOne
    public Goods goods;


    @Column(name = "lock_version")
    public int lockVersion;

    @Column(name = "created_at")
    public Date createdAt;


    public ResalerFav(User user, Goods goods) {
        this.user = user;
        this.goods = goods;
        this.lockVersion = 0;
        this.createdAt = new Date();
    }

    public static List<ResalerFav> findAll(User user){
        return ResalerFav.find("byUser", user).fetch();
    }

    /**
     * 加入或修改分销库列表
     *
     * @param user      用户
     * @param goods     商品
     */

    public static ResalerFav order(User user, Goods goods) {
        if (user == null  || goods == null) {
            return null;
        }

        ResalerFav fav = ResalerFav.find("byUserAndGoods", user, goods).first();

        //如果记录已存在，则更新记录，否则新建购物车记录
        if (fav!= null) {
            return fav;
        } else {
            return new ResalerFav(user, goods).save();
        }

    }

    /**
     * 从分销库中删除指定商品列表
     *
     * @param user     用户
     * @param goodsIds 商品列表，若未指定，则删除该用户所有的购物车条目
     * @return 成功删除的数量
     */
    public static int delete(User user, List<Long> goodsIds) {
        if (user == null || goodsIds == null || goodsIds.size() == 0) {
            return 0;
        }
        List<ResalerFav> favs = ResalerFav.find(
                "select r from ResalerFav where r.user = :user and r.goods.id in :ids", user, goodsIds).fetch();
        for (ResalerFav fav : favs){
            fav.delete();
        }
        return favs.size();
    }

}
