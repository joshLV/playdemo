package unit;

import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import models.sales.SearchHotKeywords;
import org.junit.Before;
import org.junit.Test;
import play.modules.paginate.ModelPaginator;
import play.test.UnitTest;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 11/21/12
 * Time: 2:38 PM
 */
public class SearchHotKeywordsUnitTest extends UnitTest {
    SearchHotKeywords searchHotKeywords;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();
        searchHotKeywords = FactoryBoy.create(SearchHotKeywords.class);
    }

    @Test
    public void testAddKeywords() {
        SearchHotKeywords.addKeywords("测试");
        SearchHotKeywords words = SearchHotKeywords.find("keywords", "测试").first();
        assertNotNull(words);
    }


    @Test
    public void testGetPage() {
        FactoryBoy.batchCreate(30, SearchHotKeywords.class,
                new SequenceCallback<SearchHotKeywords>() {
                    @Override
                    public void sequence(SearchHotKeywords target, int seq) {
                        target.keywords = "测试" + seq;
                        target.times = (long) seq;
                    }
                });
        ModelPaginator<SearchHotKeywords> page = SearchHotKeywords.getPage(null, 4, 10);
        assertEquals(10, page.getPageSize());
        assertEquals(4, page.getPageNumber());
        assertEquals(4, page.getPageCount());
    }

}
