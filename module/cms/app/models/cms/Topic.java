package models.cms;

import cache.CacheHelper;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.constants.PlatformType;
import org.apache.commons.lang.StringUtils;
import org.jsoup.safety.Whitelist;
import play.data.validation.InFuture;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

/**
 * 公告通知.
 * <p/>
 * User: sujie
 * Date: 4/23/12
 * Time: 10:31 AM
 */
@Entity
@Table(name = "cms_topic")
public class Topic extends Model {

    private static final long serialVersionUID = 70632320609113062L;

    @Required
//    @MinSize(10)
    @MaxSize(60)
    public String title;

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

    @Required
    @Lob
    private String content;

    public final static Whitelist HTML_WHITE_TAGS = Whitelist.basicWithImages();

    static {
        //增加可信标签到白名单
        HTML_WHITE_TAGS.addTags("embed", "object", "param", "span", "div", "table", "tbody", "tr", "td",
                "background-color", "width", "a");
        //增加可信属性
        HTML_WHITE_TAGS.addAttributes(":all", "style", "class", "id", "name");
        HTML_WHITE_TAGS.addAttributes("table", "style", "cellpadding", "cellspacing", "border", "bordercolor", "align");
        HTML_WHITE_TAGS.addAttributes("span", "style", "border", "align");
        HTML_WHITE_TAGS.addAttributes("object", "width", "height", "classid", "codebase");
        HTML_WHITE_TAGS.addAttributes("param", "name", "value");
        HTML_WHITE_TAGS.addAttributes("embed", "src", "quality", "width", "height", "allowFullScreen",
                "allowScriptAccess", "flashvars", "name", "type", "pluginspage");
        HTML_WHITE_TAGS.addAttributes("a", "href", "target");
    }

    @Enumerated(EnumType.STRING)
    public DeletedStatus deleted;

    public static final String CACHEKEY = "TOPIC";

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
        //return Jsoup.clean(content, HTML_WHITE_TAGS);
        return content;
    }

    public void setContent(String content) {
        this.content = content; //Jsoup.clean(content, HTML_WHITE_TAGS);
    }


    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type")
    public PlatformType platformType;

    @Enumerated(EnumType.STRING)
    public TopicType type;

    public static ModelPaginator getPage(int pageNumber, int pageSize, PlatformType platformType, TopicType type) {
        ModelPaginator<Topic> topicPage;
        final String orderBy = "displayOrder, type, effectiveAt desc, expireAt";
        if (platformType == null) {
            topicPage = new ModelPaginator<Topic>(Topic.class, "deleted = ?", DeletedStatus.UN_DELETED).orderBy(orderBy);
        } else if (type == null) {
            topicPage = new ModelPaginator<Topic>(Topic.class, "deleted = ? and platformType=?",
                    DeletedStatus.UN_DELETED, platformType).orderBy(orderBy);
        } else {
            topicPage = new ModelPaginator<Topic>(Topic.class, "deleted = ? and platformType=? and type=?",
                    DeletedStatus.UN_DELETED, platformType, type).orderBy(orderBy);
        }
        topicPage.setPageNumber(pageNumber);
        topicPage.setPageSize(pageSize);
        return topicPage;
    }

    public static void delete(Long id) {
        Topic topic = Topic.findById(id);
        topic.deleted = DeletedStatus.DELETED;
        topic.save();
    }

    public static void update(Long id, Topic topic) {
        Topic oldTopic = Topic.findById(id);
        oldTopic.content = topic.content;
        oldTopic.displayOrder = topic.displayOrder;
        oldTopic.effectiveAt = topic.effectiveAt;
        oldTopic.expireAt = topic.expireAt;
        oldTopic.platformType = topic.platformType;
        oldTopic.type = topic.type;
        oldTopic.title = topic.title;
        oldTopic.save();
    }

    public static List<Topic> findByType(PlatformType platformType, TopicType type, Date currentDate, int limit) {
        final String orderBy = "displayOrder, effectiveAt desc, expireAt";

        List<Topic> topics = find("deleted = ? and  platformType= ? and type = ? and effectiveAt <= ? and expireAt >= ? order by " + orderBy,
                DeletedStatus.UN_DELETED, platformType, type, currentDate, currentDate).fetch(limit);

        if (topics.size() == 0) {
            topics = Topic.find("deleted = ?  and platformType = ? and type = ? order by " + orderBy,
                    DeletedStatus.UN_DELETED, platformType, type).fetch(limit);
        }

        return topics;
    }

    public static List<Topic> findByCondition(PlatformType platformType, TopicType type) {
        final String orderBy = "displayOrder, effectiveAt desc";

        List<Topic> topics = find("deleted = ? and  platformType= ? and type = ?  order by " + orderBy,
                DeletedStatus.UN_DELETED, platformType, type).fetch();

        return topics;
    }

    @Transient
    public static Topic getTopValid(PlatformType platformType) {
        Date currentDate = new Date();
        return find("deleted=? and platformType=? and effectiveAt<=? and expireAt>=? order by id desc",
                DeletedStatus.UN_DELETED, platformType, currentDate, currentDate).first();
    }

    /**
     * 得到技术支持信息.
     * @return
     */
    @Transient
    public static Topic getDevOnCall() {
        Date currentDate = new Date();
        return find("deleted=? and platformType=? and effectiveAt<=? and expireAt>=? order by id desc",
                DeletedStatus.UN_DELETED, PlatformType.DEV_ONCALL, currentDate, currentDate).first();
    }
}
