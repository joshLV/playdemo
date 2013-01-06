package models.sales;

import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.paginate.ModelPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;


/**
 * 网站搜索的热门关键字.
 * <p/>
 * User: sujie
 * Date: 11/21/12
 * Time: 2:21 PM
 */
@Entity
@Table(name = "search_hot_keywords")
public class SearchHotKeywords extends Model {
    @Required
    public String keywords;

    @Required
    public Long times = 1l;

    @Column(name = "updated_at")
    public Date updatedAt = new Date();

    public static void addKeywords(String keywords) {
        if (StringUtils.isBlank(keywords)) {
            return;
        }
        String[] keywordArrays = keywords.split(" |,|;|，");
        for (String singleKeywords : keywordArrays) {
            SearchHotKeywords hotKeywords = SearchHotKeywords.find("keywords=?", singleKeywords).first();
            if (hotKeywords != null) {
                hotKeywords.times += 1;
                hotKeywords.updatedAt = new Date();
                hotKeywords.save();
            } else {
                SearchHotKeywords searchHotKeywords = new SearchHotKeywords();
                searchHotKeywords.keywords = keywords;
                searchHotKeywords.times = 1l;
                searchHotKeywords.create();
            }
        }
    }

    public static ModelPaginator<SearchHotKeywords> getPage(String searchKeywords, int pageNumber, int pageSize) {
        ModelPaginator page;
        if (StringUtils.isNotBlank(searchKeywords)) {
            page = new ModelPaginator(SearchHotKeywords.class, "keywords like ?", "%" + searchKeywords + "%").orderBy("times desc");
        } else {
            page = new ModelPaginator(SearchHotKeywords.class).orderBy("times desc");
        }
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }
}
