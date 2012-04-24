package models.cms;

import org.apache.commons.lang.StringUtils;
import com.uhuila.common.constants.PlatformType;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.data.validation.InFuture;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * 公告通知.
 * <p/>
 * User: sujie
 * Date: 4/23/12
 * Time: 10:31 AM
 */
@Entity
@Table(name = "notice")
public class Topic extends Model {
    @Required
    @MinSize(10)
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
    @MinSize(7)
    @MaxSize(4000)
    private String content;
    public final static Whitelist HTML_WHITE_TAGS = Whitelist.relaxed();
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


    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type")
    public PlatformType platformType;

    @Enumerated(EnumType.STRING)
    public TopicType type;

    public static ModelPaginator getPage(int pageNumber, int pageSize, PlatformType platformType, TopicType type) {
        ModelPaginator<Topic> topicPage;
        final String orderBy = "displayOrder, type, " +
                "effectiveAt,expireAt";
        if (platformType == null) {
            topicPage = new ModelPaginator<Topic>(Topic.class).orderBy(orderBy);
        } else if (type == null) {
            topicPage = new ModelPaginator<Topic>(Topic.class, "platformType=?", platformType).orderBy(orderBy);
        } else {
            topicPage = new ModelPaginator<Topic>(Topic.class, "platformType=? and type=?", platformType,
                    type).orderBy(orderBy);
        }
        topicPage.setPageNumber(pageNumber);
        topicPage.setPageSize(pageSize);
        return topicPage;
    }

    public static void delete(Long id) {
        Topic topic = Topic.findById(id);
        if (topic!= null){
            topic.delete();
        }
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
}
