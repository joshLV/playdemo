package models.supplier;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

import cache.CacheHelper;
import com.uhuila.common.util.PathUtil;
import play.Play;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.Date;

/**
 * TODO.
 * <p/>
 * User: wangjia
 * Date: 13-1-24
 * Time: 上午11:02
 */

@Table(name = "supplier_contract_images")
@Entity
public class SupplierContractImage extends Model {
    private static final long serialVersionUID = 4063131063912510682L;
    @ManyToOne
    public Supplier supplier;

    public static final String IMAGE_TINY = "60x46_nw";
    public static final String IMAGE_SMALL = "172x132";
    public static final String IMAGE_MIDDLE = "234x178";
    public static final String IMAGE_LARGE = "340x260";
    public static final String IMAGE_LOGO = "300x180_nw";
    public static final String IMAGE_SLIDE = "nw";
    public static final String IMAGE_ORIGINAL = "nw";
    public static final String IMAGE_DEFAULT = "";
    public static final String IMAGE_SERVER = Play.configuration.getProperty
//            ("image.server", "img0.uhcdn.com");
            ("image.server", "");

    /**
     * 图片路径
     */
    @Column(name = "image_path")
    public String imagePath;

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

    public static final String CACHEKEY_SUPPLIERID = "SUPPLIER_ID";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_SUPPLIERID + this.supplier.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_SUPPLIERID + this.supplier.id);
        super._delete();
    }

    public SupplierContractImage(Supplier supplier, String imagePath) {
        this.supplier = supplier;
        this.imagePath = imagePath;
        this.createdAt = new Date();
    }
}
