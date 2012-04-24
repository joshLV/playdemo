package controllers;

import com.uhuila.common.constants.PlatformType;
import models.cms.Topic;
import models.cms.TopicType;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

import static play.Logger.warn;

/**
 * 公告通知发布.
 * <p/>
 * User: sujie
 * Date: 4/23/12
 * Time: 10:51 AM
 */
@With(OperateRbac.class)
@ActiveNavigation("topics_index")
public class OperateTopics extends Controller {
    private static final int PAGE_SIZE = 15;

    public static void index(PlatformType platformType, TopicType type) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        ModelPaginator topicPage = Topic.getPage(pageNumber, PAGE_SIZE, platformType, type);

        render(topicPage, platformType, type);
    }

    @ActiveNavigation("topics_add")
    public static void add() {
        render();
    }

    public static void create(@Valid Topic topic) {
        checkExpireAt(topic);
        if (Validation.hasErrors()) {
            render("OperateTopics/add.html",topic);
        }
        topic.create();
        index(null, null);
    }

    private static void checkExpireAt(Topic topic) {
        if (topic.effectiveAt != null && topic.expireAt != null && topic.expireAt.before(topic.effectiveAt)) {
            Validation.addError("topic.expireAt", "validation.beforeThanEffectiveAt");
        }
    }

    public static void edit(Long id) {
        Topic topic = Topic.findById(id);
        render(topic);
    }

    public static void update(Long id, Topic topic) {
        checkExpireAt(topic);
        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            render("OperateTopics/edit.html", topic);
        }
        Topic.update(id,topic);

        index(null, null);
    }

    public static void delete(Long id) {
        Topic.delete(id);
        index(null, null);
    }
}
