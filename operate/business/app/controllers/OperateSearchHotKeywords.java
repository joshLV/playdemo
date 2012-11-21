package controllers;

import models.sales.SearchHotKeywords;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 热门关键词查询.
 * <p/>
 * User: sujie
 * Date: 11/21/12
 * Time: 3:04 PM
 */
@With(OperateRbac.class)
@ActiveNavigation("hot_keywords")
public class OperateSearchHotKeywords extends Controller {

    private static final int PAGE_SIZE = 20;

    public static void index(String searchKeywords) {
        int pageNumber = getPage();
        ModelPaginator<SearchHotKeywords> searchKeywordsPage = SearchHotKeywords.getPage(searchKeywords, pageNumber, PAGE_SIZE);

        render(searchKeywords, searchKeywordsPage);
    }


    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

}