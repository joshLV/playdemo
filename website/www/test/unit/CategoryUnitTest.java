package unit;

import models.sales.Category;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

/**
 * 分类的单元测试.
 * <p/>
 * User: sujie
 * Date: 3/16/12
 * Time: 4:49 PM
 */
public class CategoryUnitTest extends UnitTest {
    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        Fixtures.delete(Category.class);
        Fixtures.loadModels("fixture/categories.yml");
    }

    @Test
    public void testFindTopByBrand() {
        int limit = 1;
        long categoryId = (Long) Fixtures.idCache.get("models.sales.Category-models_sales_Category_1");

        List<Category> categoryList = Category.findTop(limit, categoryId);

        assertEquals(1, categoryList.size());
    }

    @Test
    public void testFindByParent() {
        long categoryId = (Long) Fixtures.idCache.get("models.sales.Category-models_sales_Category_1");
        List<Category> categoryList = Category.findByParent(categoryId);
        assertEquals(2, categoryList.size());

        categoryList = Category.findByParent(1, categoryId);
        assertEquals(1, categoryList.size());
    }

}
