package models.resale;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.order.OuterOrderPartner;
import models.sales.Goods;
import models.sales.GoodsStatus;
import models.sales.MaterialType;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Column(name = "taobao_item_id")
    public Long taobaoItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "partner")
    public OuterOrderPartner partner;      //合作伙伴

    /**
     * 第三方上架时间.
     */
    public Date onsaledAt;

    /**
     * 第三方下架时间.
     */
    public Date offSaleAt;
    /**
     * 第三方发布城市.
     */
    @Column(name = "third_city")
    public String thirdCity;

    /**
     * 第三方url.
     */
    @Column(name = "third_url")
    public String thirdUrl;
    /**
     * 第三方团购ID.
     */
    @Column(name = "third_groupbuy_id")
    public Long thirdGroupbuyId;

    @Column(name = "last_link_id")
    public Long lastLinkId;//记录最后一次推送的linkId

    /**
     * 第三方状态.
     */
    public String outerStatus;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;

    public ResalerFav(Resaler resaler, Goods goods) {
        this.resaler = resaler;
        this.goods = goods;
        this.lockVersion = 0;
        this.deleted = DeletedStatus.UN_DELETED;
        this.createdAt = new Date();
    }

    public static List<ResalerFav> findAll(Resaler resaler) {
        return ResalerFav.find(" resaler= ? and goods.materialType = ?", resaler, MaterialType.ELECTRONIC).fetch();
    }

    /**
     * 查询分销库
     *
     * @param resaler        分销商
     * @param createdAtBegin 下单开始时间
     * @param createdAtEnd   下单结束时间
     * @param goodsName      商品名
     * @return sql 查询条件
     */
    public static List<ResalerFav> findFavs(Resaler resaler, Date createdAtBegin, Date createdAtEnd, String goodsName) {
        return findFavs(resaler, createdAtBegin, createdAtEnd, goodsName, null);
    }

    public static List<ResalerFav> findFavs(Resaler resaler, Date createdAtBegin, Date createdAtEnd, String goodsName, Long goodsId) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> paramsMap = new HashMap<>();
        sql.append("select f from ResalerFav f where 1=1");

        sql.append(" and f.deleted = :deleted");
        paramsMap.put("deleted", DeletedStatus.UN_DELETED);

        sql.append(" and f.goods.materialType = :materialType");
        paramsMap.put("materialType", MaterialType.ELECTRONIC);

        sql.append(" and f.goods.status = :status");
        paramsMap.put("status", GoodsStatus.ONSALE);

        sql.append(" and f.goods.expireAt >= :expireAt");
        paramsMap.put("expireAt", new Date());

        if (resaler != null) {
            sql.append(" and f.resaler = :resaler");
            paramsMap.put("resaler", resaler);
        }
        if (createdAtBegin != null) {
            sql.append(" and f.createdAt >= :createdAtBegin");
            paramsMap.put("createdAtBegin", createdAtBegin);
        }
        if (createdAtEnd != null) {
            sql.append(" and f.createdAt <= :createdAtEnd");
            paramsMap.put("createdAtEnd", DateUtil.getEndOfDay(createdAtEnd));
        }

        if (goodsId != null && goodsId > 0l) {
            sql.append(" and f.goods.id = :goodsId");
            paramsMap.put("goodsId", goodsId);
        }


        //按照商品名称检索
        if (StringUtils.isNotBlank(goodsName)) {
            sql.append(" and f.goods.name like :goodsName)");
            paramsMap.put("goodsName", "%" + goodsName + "%");
        }
        sql.append(" order by f.createdAt desc");
        EntityManager entityManager = JPA.em();
        Query result = entityManager.createQuery(sql.toString());
        for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
            result.setParameter(entry.getKey(), entry.getValue());
        }
        return result.getResultList();
    }

    /**
     * 从分销库中删除指定商品列表
     *
     * @param resaler  用户
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

        for (ResalerFav fav : favs) {
            fav.deleted = DeletedStatus.DELETED;
            fav.save();
        }
        return favs.size();
    }

    /**
     * 加入或修改分销库列表
     *
     * @param resaler  用户
     * @param goodsIds 商品标识
     */
    public static Map<String, String> checkGoods(Resaler resaler,
                                                 Long[] goodsIds) {
        Map<String, String> map = new HashMap();
        String ids = "";
        if (goodsIds != null) {
            for (long goodsId : goodsIds) {
                models.sales.Goods goods = models.sales.Goods.findById(goodsId);
                ResalerFav fav =  ResalerFav.findByGoodsId(resaler, goods.id);
                //如果记录不存在，则新建记录
                if (fav != null) {
                    map.put("isExist", "1");
                } else {
                    new ResalerFav(resaler, goods).save();
                }

                ids += goodsId + ",";
            }
        }
        map.put("goodsId", !"".equals(ids) ? ids.substring(0, ids.length() - 1) : "");
        return map;
    }

    public static ResalerFav findByGoodsId(Resaler resaler, Long goodsId) {
        return ResalerFav.find("resaler = ? and goods.id = ? and deleted = ?",
                resaler, goodsId, DeletedStatus.UN_DELETED).first();
    }
}
