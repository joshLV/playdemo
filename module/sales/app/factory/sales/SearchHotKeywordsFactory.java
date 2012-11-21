package factory.sales;

import factory.ModelFactory;
import models.sales.SearchHotKeywords;

import java.util.Date;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 11/21/12
 * Time: 2:34 PM
 */
public class SearchHotKeywordsFactory extends ModelFactory<SearchHotKeywords> {
    @Override
    public SearchHotKeywords define() {
        SearchHotKeywords hotKeywords = new SearchHotKeywords();
        hotKeywords.times = 3l;
        hotKeywords.keywords = "川菜";
        hotKeywords.updatedAt = new Date();
        return hotKeywords;
    }

}
