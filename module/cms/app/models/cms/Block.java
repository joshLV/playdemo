package models.cms;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.Play;
import play.data.validation.InFuture;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;
import util.DateHelper;
import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import com.uhuila.common.util.PathUtil;

/**
 * 内容管理系统之内容块定义。
 *
 * @author <a href="mailto:tangliqun@uhuila.com">唐力群</a>
 */
@Entity
@Table(name = "cms_block")
public class Block extends Model {

    private static final long serialVersionUID = 701232063912330652L;

    public final static Whitelist HTML_WHITE_TAGS = Whitelist.relaxed();

    private static final String IMAGE_SERVER = Play.configuration.getProperty
            ("image.server", "img0.uhcdn.com");
    public static final String IMAGE_TINY = "60x46_nw";
    public static final String IMAGE_SMALL = "172x132";
    public static final String IMAGE_MIDDLE = "234x178";
    public static final String IMAGE_LARGE = "340x260";
    public static final String IMAGE_LOGO = "300x180_nw";
    public static final String IMAGE_SLIDE = "nw";
    public static final String IMAGE_ORIGINAL = "nw";
    public static final String IMAGE_DEFAULT = "";

    @Required
    @MinSize(1)
    @MaxSize(60)
    public String title;

    public String link;

    @Column(name = "image_url")
    public String imageUrl;

    /**
     * 有效开始日
     */
    @Required
    @Column(name = "effective_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date effectiveAt;

    /**
     * 有效结束日
     */
    @Required
    @InFuture
    @Column(name = "expire_at")
    @Temporal(TemporalType.TIMESTAMP)
    public Date expireAt;

    public Integer displayOrder;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    public BlockType type;

    @Enumerated(EnumType.STRING)
    public DeletedStatus deleted;

    @Transient
    public String getShowImageUrl() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imageUrl, IMAGE_SLIDE);
    }

    @Transient
    public String getShowImageUrlMiddle() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imageUrl, IMAGE_MIDDLE);
    }

    @Transient
    public String getShowImageUrlTiny() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imageUrl, IMAGE_TINY);
    }

    @Transient
    public String getShowImageUrlOrigin() {
        return PathUtil.getImageUrl(IMAGE_SERVER, imageUrl, IMAGE_ORIGINAL);
    }

    public static final String CACHEKEY = "BLOCK";

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._save();
    }

    @Override
    public void _delete() {
        CacheHelper.delete(CACHEKEY);
        CacheHelper.delete(CACHEKEY + this.id);
        super._delete();
    }

    /**
     * 公告内容
     *
     * @return
     */
    public String getContent() {
        if (StringUtils.isBlank(content) || "<br />".equals(content)) {
            return "";
        }
        return Jsoup.clean(content, HTML_WHITE_TAGS);
    }

    public void setContent(String content) {
        this.content = Jsoup.clean(content, HTML_WHITE_TAGS);
    }

    public static ModelPaginator getPage(int pageNumber, int pageSize, BlockType type) {
        ModelPaginator<Block> blockPage;
        final String orderBy = "type, expireAt desc, effectiveAt desc, displayOrder";
        if (type == null) {
            blockPage = new ModelPaginator<Block>(Block.class, "deleted = ?",
                    DeletedStatus.UN_DELETED).orderBy(orderBy);
        } else {
            blockPage = new ModelPaginator<Block>(Block.class, "deleted = ? and type=?",
                    DeletedStatus.UN_DELETED, type).orderBy(orderBy);
        }
        blockPage.setPageNumber(pageNumber);
        blockPage.setPageSize(pageSize);
        return blockPage;
    }

    public static void delete(Long id) {
        Block block = Block.findById(id);
        block.deleted = DeletedStatus.DELETED;
        block.save();
    }

    public static void update(Long id, Block block) {
        Block oldBlock = Block.findById(id);
        oldBlock.content = block.content;
        oldBlock.displayOrder = block.displayOrder;
        oldBlock.effectiveAt = block.effectiveAt;
        oldBlock.expireAt = block.expireAt;
        oldBlock.title = block.title;
        oldBlock.link = block.link;
        oldBlock.imageUrl = block.imageUrl;
        oldBlock.type = block.type;
        oldBlock.save();
    }

    /**
     * 按BlockType和时间查询可用的Block，如果找不到任何记录，则按BlockType查询.
     *
     * @param type
     * @param currentDate
     * @return
     */
    public static List<Block> findByType(BlockType type, Date currentDate) {
        final String orderBy = "displayOrder, effectiveAt desc, expireAt";

        List<Block> blocks = Block.find("deleted = ? and type = ? and effectiveAt <= ? and expireAt >= ? order by " + orderBy,
                DeletedStatus.UN_DELETED, type, currentDate, DateHelper.beforeDays(currentDate, 1)).fetch();

        //如果都过期了，取一个最近的日期进行显示
        if (blocks.size() == 0) {
            blocks = Block.find("deleted = ? and type = ? order by expireAt desc",
                    DeletedStatus.UN_DELETED, type).fetch(1);
        }
        return blocks;
    }

    public static List<Block> findLastByType(BlockType websiteSlide, int i) {
        return null;
    }

    public long totalClickedCount() {
        return BlockClickTrack.count("block=?", this);
    }

    public long todayClickedCount() {
        return BlockClickTrack.count("block=? and createdAt >= ?", this, DateUtil.getBeginOfDay(new Date()));
    }
}
