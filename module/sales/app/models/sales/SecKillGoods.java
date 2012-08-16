package models.sales;

import com.uhuila.common.util.PathUtil;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.Play;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-15
 * Time: 上午10:09
 */
@Entity
@Table(name = "seckill_goods")
public class SecKillGoods extends Model {
    public static final String IMAGE_TINY = "60x46_nw";
    public static final String IMAGE_SMALL = "172x132";
    public static final String IMAGE_MIDDLE = "234x178";
    public static final String IMAGE_LARGE = "340x260";
    public static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.uhcdn.com");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_id", nullable = true)
    public Goods goods;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "secKillGoods")
    @OrderBy("id")
    public List<SecKillGoodsItem> secKillGoodsItemList;
    /**
     * 限购数量
     */
    @Required
    @Column(name = "person_limit_number")
    public Integer personLimitNumber = 0;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;

    /**
     * 原始图片路径
     */
    @Column(name = "image_path")
    public String imagePath;

    public final static Whitelist HTML_WHITE_TAGS = Whitelist.relaxed();

    static {
        //增加可信标签到白名单
        HTML_WHITE_TAGS.addTags("embed", "object", "param", "span", "div", "table", "tbody", "tr", "td",
                "background-color", "width", "figure", "figcaption", "strong", "p", "dl", "dt", "dd");
        //增加可信属性
        HTML_WHITE_TAGS.addAttributes(":all", "style", "class", "id", "name");
        HTML_WHITE_TAGS.addAttributes("table", "style", "cellpadding", "cellspacing", "border", "bordercolor", "align");
        HTML_WHITE_TAGS.addAttributes("span", "style", "border", "align");
        HTML_WHITE_TAGS.addAttributes("object", "width", "height", "classid", "codebase");
        HTML_WHITE_TAGS.addAttributes("param", "name", "value");
        HTML_WHITE_TAGS.addAttributes("embed", "src", "quality", "width", "height", "allowFullScreen",
                "allowScriptAccess", "flashvars", "name", "type", "pluginspage");
    }

    /**
     * 温馨提示
     */
    @MaxSize(65000)
    @Lob
    private String prompt;

    public String getPrompt() {
        if (StringUtils.isBlank(prompt)) {
            return "";
        }
        return Jsoup.clean(prompt, HTML_WHITE_TAGS);
    }

    public void setPrompt(String prompt) {
        this.prompt = Jsoup.clean(prompt, HTML_WHITE_TAGS);
    }

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

    public static JPAExtPaginator<SecKillGoods> findByCondition(SecKillGoodsCondition condition, int pageNumber, int pageSize) {
        JPAExtPaginator<SecKillGoods> goodsPage = new JPAExtPaginator<>
                ("SecKillGoods g", "g", SecKillGoods.class, condition.getFilter(),
                        condition.getParamMap())
                .orderBy("g.createdAt desc");
        goodsPage.setPageNumber(pageNumber);
        goodsPage.setPageSize(pageSize);
        goodsPage.setBoundaryControlsEnabled(false);

        return goodsPage;
    }

    public static void update(Long id, SecKillGoods secKillGoods) {
        SecKillGoods dbSecKillGoods = SecKillGoods.findById(id);
        dbSecKillGoods.personLimitNumber = secKillGoods.personLimitNumber;
        if (secKillGoods.imagePath != null)
            dbSecKillGoods.imagePath = secKillGoods.imagePath;
        dbSecKillGoods.prompt = secKillGoods.prompt;
        dbSecKillGoods.save();
    }
}
