package models.sales;

import cache.CacheHelper;
import com.uhuila.common.util.PathUtil;
import play.Play;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * <p/>
 * User: yanjy
 * Date: 12-10-22
 * Time: 下午5:30
 */
@Table(name = "goods_images")
@Entity
public class GoodsImages extends Model {
    private static final long serialVersionUID = 4063131063912510682L;
    @ManyToOne
    public Goods goods;

    @ManyToOne
    public GoodsHistory goodsHistory;

    public static final String IMAGE_TINY = "60x46_nw";
    public static final String IMAGE_SMALL = "172x132";
    public static final String IMAGE_MIDDLE = "234x178";
    public static final String IMAGE_LARGE = "340x260";
    public static final String IMAGE_LOGO = "300x180_nw";
    public static final String IMAGE_SLIDE = "nw";
    public static final String IMAGE_ORIGINAL = "nw";
    public static final String IMAGE_DEFAULT = "";
    public static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.uhcdn.com");
    /**
     * 图片路径
     */
    @Column(name = "image_path")
    public String imagePath;

    @Column(name = "is_display_site")
    public Boolean isDisplaySite;

    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 最小规格图片路径
     */
    @Transient
    public String getImageTinyPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_TINY);
    }

    /**
     * 小规格图片路径
     */
    @Transient
    public String getImageSmallPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_SMALL);
    }


    /**
     * 中等规格图片路径
     */
    @Transient
    public String getImageMiddlePath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_MIDDLE);
    }

    /**
     * 大规格图片路径
     */
    @Transient
    public String getImageLargePath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_LARGE);
    }

    @Transient
    public String getImageOriginalPath() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imagePath, IMAGE_ORIGINAL);
    }
   public static final String CACHEKEY = "IMAGE";

    public static final String CACHEKEY_GOODSID = "GOODS_ID";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_GOODSID + this.goods.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_GOODSID + this.goods.id);
        super._delete();
    }

    public GoodsImages(Goods goods, String imagePath) {
        this.goods = goods;
        this.imagePath = imagePath;
        this.isDisplaySite = false;
        this.createdAt = new Date();
    }


}
