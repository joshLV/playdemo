package unit;

import models.sales.Category;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

import factory.FactoryBoy;
import factory.callback.SequenceCallback;

/**
 * 分类的单元测试.
 * <p/>
 * User: sujie
 * Date: 3/16/12
 * Time: 4:49 PM
 */
public class CategoryUnitTest extends UnitTest {
    Category category;
    List<Category> categories;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        FactoryBoy.deleteAll();
        category = FactoryBoy.create(Category.class);
        categories = FactoryBoy.batchCreate(8, Category.class,
                new SequenceCallback<Category>() {
                    @Override
                    public void sequence(Category target, int seq) {
                        target.name = "Test#" + seq;
                        target.parentCategory = category;
                    }
                });
    }

    @Test
    public void testFindTopByBrand() {
        int limit = 1;
        List<Category> categoryList = Category.findTop(limit, category.id);
        System.out.println("size??" + Category.count());
        assertEquals(1, categoryList.size());
    }

    @Test
    public void testFindByParent() {
        List<Category> categoryList = Category.findByParent(category.id);
        assertEquals(Category.count() - 1, categoryList.size());

        categoryList = Category.findByParent(1, category.id);
        assertEquals(1, categoryList.size());
    }


}
