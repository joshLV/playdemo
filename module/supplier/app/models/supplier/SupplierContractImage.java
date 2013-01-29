package models.supplier;

import org.apache.commons.codec.digest.DigestUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO.
 * <p/>
 * User: wangjia
 * Date: 13-1-24
 * Time: 上午11:02
 */

@Table(name = "supplier_contract_image")
@Entity
public class SupplierContractImage extends Model {
    private static final long serialVersionUID = 4063131063912510682L;

    @ManyToOne
    public SupplierContract contract;

    public static final String IMAGE_TINY = "60x46_nw";
    public static final String IMAGE_SMALL = "172x132";
    public static final String IMAGE_MIDDLE = "234x178";
    public static final String IMAGE_LARGE = "340x260";
    public static final String IMAGE_LOGO = "300x180_nw";
    public static final String IMAGE_SLIDE = "nw";
    public static final String IMAGE_ORIGINAL = "nw";
    public static final String IMAGE_DEFAULT = "";


    //    private static final Pattern IMAGE_PATTERN = Pattern.compile("^/([0-9]+)/([0-9]+)/([0-9]+)/(.+).((?i)(jpg|png|gif|jpeg))$");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("^/([0-9]+)/([0-9]+)/(.+).((?i)(jpg|png|gif|jpeg))$");

    private static final String IMAGE_ROOT_GENERATED = "/contract/p";

    private static final Pattern FILENAME_PATTERN = Pattern.compile("(.*/)*(.*)$");
    private static final String SIZE_KEY = "sJ34fds29h@d";


    public static final String BASE_URL = Play.configuration.getProperty("uri.operate_business");


    /**
     * 图片路径
     * GET /contract/images/{supplier_id}/{contract_id}/{id}.jpg
     * <p/>
     * File save to:  /nfs/images/contract/o/{supplier_id}/{contract_id}/{imageFileName}
     * imageFileName
     */

    @Column(name = "image_path")
    public String imagePath;

    @Column(name = "shown_name")
    public String shownName;

    @Column(name = "created_at")
    public Date createdAt;

    public String description;

    /**
     * 最小规格图片路径
     */
    @Transient
    public String getImageTinyPath() {
        return getImageUrl(BASE_URL, imagePath, IMAGE_TINY);
    }

    /**
     * 小规格图片路径
     */
    @Transient
    public String getImageSmallPath() {
        return getImageUrl(BASE_URL, imagePath, IMAGE_SMALL);
    }


    /**
     * 中等规格图片路径
     */
    @Transient
    public String getImageMiddlePath() {
        return getImageUrl(BASE_URL, imagePath, IMAGE_MIDDLE);
    }

    /**
     * 大规格图片路径
     */
    @Transient
    public String getImageLargePath() {
        return getImageUrl(BASE_URL, imagePath, IMAGE_LARGE);
    }

    @Transient
    public String getImageOriginalPath() {
        return getImageUrl(BASE_URL, imagePath, IMAGE_ORIGINAL);
    }

    public static final String CACHEKEY = "IMAGE";

    //    public static final String CACHEKEY_SUPPLIERID = "SUPPLIER_ID";
    public static final String CACHEKEY_SUPPLIER_CONTRACT_ID = "SUPPLIER_CONTRACT_ID";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_SUPPLIER_CONTRACT_ID + this.contract.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        CacheHelper.delete(CACHEKEY_SUPPLIER_CONTRACT_ID + this.contract.id);
        super._delete();
    }

    public SupplierContractImage(Supplier supplier, SupplierContract contract, String shownName, String imagePath) {
//        this.supplierContract = new SupplierContract(supplier).save();
        this.contract = contract;
        this.imagePath = imagePath;
        this.shownName = shownName;
        this.createdAt = new Date();
    }


    /**
     * 根据图片服务器以及图片路径生成图片的url.
     *
     * @param imagePath 图片路径
     * @param fix       图片大小规格
     * @return 完整的图片url
     */
    public static String getImageUrl(String baseUrl, String imagePath, String fix) {
        if (baseUrl == null || imagePath == null) {
            return null;
        }

        if (fix == null) {
            fix = "";
        }

        Matcher matcher = IMAGE_PATTERN.matcher(imagePath);
        if (!matcher.matches()) {
            return null;
        }

        String fixName = fix.equals("") ? "" : "_" + fix;

//        String newFileName = matcher.group(4) + fixName + "." + matcher.group(5);
        String newFileName = matcher.group(3) + fixName + "." + matcher.group(4);
        String value = IMAGE_ROOT_GENERATED + signImgPath(
                "/" + matcher.group(1)
                        + "/" + matcher.group(2)
//                        + "/" + matcher.group(3)
                        + "/" + newFileName
        );
        return baseUrl + IMAGE_ROOT_GENERATED + signImgPath(
                "/" + matcher.group(1)
                        + "/" + matcher.group(2)
//                        + "/" + matcher.group(3)
                        + "/" + newFileName
        );
    }

    /**
     * 请求加密后的图片路径
     *
     * @param requestUri 文件路径 接受 /a/b/c.abc  /a.bc  a.bc 等不同形式的参数
     * @return 请求签名
     */
    public static String signImgPath(String requestUri) {
        Matcher matcher = FILENAME_PATTERN.matcher(requestUri);
        if (!matcher.matches()) {
            return null;
        }
        String pre = matcher.group(1) == null ? "" : matcher.group(1);
        String sign = imgSign(matcher.group(2));
        return pre + sign + "_" + matcher.group(2);
    }

    public static String imgSign(String fileName) {
        return DigestUtils.md5Hex(fileName + "-" + SIZE_KEY).substring(24);
    }

}
